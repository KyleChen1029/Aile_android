package tw.com.chainsea.ce.sdk.service;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.network.NetworkHelper;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
import tw.com.chainsea.ce.sdk.bean.todo.Type;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.SyncTodoRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.TodoCompleteRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.TodoCreateRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.TodoUpdateRequest;
import tw.com.chainsea.ce.sdk.reference.BusinessReference;
import tw.com.chainsea.ce.sdk.reference.TodoReference;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.custom.view.alert.AlertView;


/**
 * current by evan on 2020-07-14
 *
 * @author Evan Wang
 * date 2020-07-14
 */
public class TodoService {


    /**
     * Calculate the timeout number of to-do items, including work list items
     * *** Because the end time format of the business opportunity entity is a string, it cannot be judged
     * update 2020/12/30
     */
    public static void getExpiredCount(Context context) {
        int businessExpired = 0;
        if (UserPref.getInstance(context).hasBusinessSystem()) {
            businessExpired = BusinessReference.getExpiredCount(null);
        }
        int todoExpired = TodoReference.getExpiredCount(null);
        EventBusUtils.sendEvent(new EventMsg(MsgConstant.UPDATE_TODO_EXPIRED_COUNT_EVENT, todoExpired + businessExpired));
    }

    public static void getTodoEntities(Context context, String roomId, RefreshSource source, @Nullable ServiceCallBack<List<TodoEntity>, RefreshSource> callBack) {
        String personRoomId = UserPref.getInstance(context).getPersonRoomId();
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            if (callBack != null) {
                ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                    List<TodoEntity> localEntities = Strings.isNullOrEmpty(roomId) ? TodoReference.findAll(null) : personRoomId.equals(roomId) ? TodoReference.findBySelf(null, roomId) : TodoReference.findByRoomId(null, roomId);
                    callBack.complete(filter(localEntities, ProcessStatus.UN_SYNC_DELETE), RefreshSource.LOCAL);
                });
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doSyncTodo(context, new SyncTodoRequest.Listener() {
                @Override
                public void onSuccess() {
                    List<TodoEntity> list = Strings.isNullOrEmpty(roomId) ? TodoReference.findAll(null) : personRoomId.equals(roomId) ? TodoReference.findBySelf(null, roomId) : TodoReference.findByRoomId(null, roomId);
                    for (TodoEntity entity : list) {
                        if (TodoStatus.PROGRESS.equals(entity.getStatus())) {
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_UPDATE_ALARM, entity.toJson()));
                        } else {
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_DELETE_ALARM, entity.toJson()));
                        }
                    }
                    if (callBack != null) {
                        callBack.complete(filter(list, ProcessStatus.UN_SYNC_DELETE), RefreshSource.REMOTE);
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (callBack != null) {
                        callBack.error(errorMessage);
                    }
                }
            });
        }
    }

    private static List<TodoEntity> filter(List<TodoEntity> entities, ProcessStatus status) {
        List<TodoEntity> list = Lists.newLinkedList();
        Iterator<TodoEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            TodoEntity entity = iterator.next();
            if (status.equals(entity.getProcessStatus()) || TodoStatus.DELETED.equals(entity.getStatus())) {
            } else {
                list.add(entity);
            }
        }
        return list;
    }

    public static void doSync(Context context) {
        List<TodoEntity> notYetSyncEntities = TodoReference.findNotProcessStatus(null, ProcessStatus.UNDEF);
        if (NetworkHelper.hasNetWork(context)) {
            sync(context, notYetSyncEntities, null);
        }
    }

    private synchronized static void sync(Context context, List<TodoEntity> entities, @Nullable ServiceCallBack<TodoEntity, RefreshSource> callBack) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            for (TodoEntity entity : entities) {
                if (TodoStatus.DELETED.equals(entity.getStatus())) {
                    deleteAndSync(context, entity, RefreshSource.REMOTE);
                    TodoReference.delete(null, entity.getId());
                    continue;
                }
                switch (entity.getProcessStatus()) {
                    case UN_SYNC_CREATE:
                        saveAndSync(context, entity, RefreshSource.REMOTE, callBack);
                        break;
                    case UN_SYNC_UPDATE:
//                        updateAndSync(context, entity, RefreshSource.REMOTE);
                        break;
                    case UN_SYNC_DELETE:
//                        deleteAndSync(context, entity, RefreshSource.REMOTE);
                        break;
                    case UN_SYNC_COMPLETE:
//                        completeAndSync(context, entity, RefreshSource.REMOTE);
                    case UNDEF:
                        break;
                }
            }
        });
    }

    /**
     * Add ToDo & Sync Server
     */
    public static void saveAndSync(Context context, TodoEntity entity, RefreshSource source, @Nullable ServiceCallBack<TodoEntity, RefreshSource> callBack) {
        entity.setProcessStatus(ProcessStatus.UNDEF.equals(entity.getProcessStatus()) ? ProcessStatus.UN_SYNC_CREATE : entity.getProcessStatus());
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            boolean saveStatus = TodoReference.save(null, entity);
            if (saveStatus && callBack == null) {
                TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, localEntity.toJson()));
            } else if (saveStatus) {
                TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                callBack.complete(localEntity, RefreshSource.LOCAL);
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doTodoCreate(context, entity, new TodoCreateRequest.Listener() {
                @Override
                public void onSuccess(String todoId, long updateTime) {
                    entity.setUpdateTime(updateTime);
                    entity.setCreateTime(updateTime);
                    entity.setProcessStatus(ProcessStatus.UNDEF);
                    boolean saveStatus = TodoReference.save(null, entity);
                    if (saveStatus && callBack == null) {
                        TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                        if(localEntity != null) {
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, localEntity.toJson()));
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_UPDATE_ALARM, localEntity.toJson()));
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
                        }
                    } else if (saveStatus) {
                        TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                        if(localEntity != null){
                            callBack.complete(localEntity, RefreshSource.LOCAL);
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_UPDATE_ALARM, localEntity.toJson()));
                        }
                    }
                    if (saveStatus) {
                        toast(context, context.getString(R.string.todo_reminder_created));
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void updateAndSync(Context context, TodoEntity entity, RefreshSource source) {
        entity.setProcessStatus(ProcessStatus.UNDEF.equals(entity.getProcessStatus()) ? ProcessStatus.UN_SYNC_UPDATE : entity.getProcessStatus());
        entity.setStatus(TodoStatus.PROGRESS);
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            boolean saveStatus = TodoReference.update(null, entity);
            if (saveStatus) {
                TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, localEntity.toJson()));
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            update(context, entity);
        }
    }

    private synchronized static void update(Context context, TodoEntity entity) {
        ApiManager.doTodoUpdate(context, entity, new TodoUpdateRequest.Listener() {
            @Override
            public void onSuccess(String todoId, long updateTime) {
                if (entity.getStatus() == TodoStatus.DONE) {
                    toast(context, context.getString(R.string.todo_reminder_continue));
                    entity.setStatus(TodoStatus.PROGRESS);
                } else {
                    toast(context, context.getString(R.string.todo_reminder_update));
                }
                entity.setUpdateTime(updateTime);
                entity.setProcessStatus(ProcessStatus.UNDEF);
                boolean saveStatus = TodoReference.save(null, entity);
                if (saveStatus) {
                    TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, localEntity.toJson()));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_UPDATE_ALARM, localEntity.toJson()));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
//                    toast(context, context.getString(R.string.todo_reminder_update));
                }
                getExpiredCount(context);
            }

            @Override
            public void onFailed(String errorMessage) {
                if (!(context instanceof Activity)) return;
                new AlertView.Builder()
                        .setContext(context)
                        .setStyle(AlertView.Style.Alert)
                        .setTitle(context.getString(R.string.warning_update_failed))
                        .setMessage(errorMessage)
                        .setCancelText(context.getString(R.string.alert_confirm))
                        .build()
                        .setCancelable(true)
                        .show();
            }
        });
    }

    // test
    public static void deleteAll(Context context) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            List<TodoEntity> entities = TodoReference.findAll(null);
            for (TodoEntity entity : entities) {
                CELog.e("");
//                deleteAndSync(context, entity, RefreshSource.ALL);
            }
        });
//        Executors.newCachedThreadPool().execute(() -> {
//
//        });
    }

    public static void deleteByProcessStatus(Context context, TodoStatus status) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            List<TodoEntity> entities = TodoReference.findByStatus(null, status);
            for (TodoEntity entity : entities) {
                CELog.e("");
//                deleteAndSync(context, entity, RefreshSource.ALL);
                deleteAndSync(context, entity, RefreshSource.REMOTE);
            }
        });
    }

    public static void deleteAndSync(Context context, TodoEntity entity, RefreshSource source) {
        entity.setStatus(TodoStatus.DELETED);
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            boolean deleteStatus;
            if (ProcessStatus.UNDEF.equals(entity.getProcessStatus())) {
                // need sync to remote
                deleteStatus = TodoReference.updateProcessStatus(null, entity.getId(), ProcessStatus.UN_SYNC_DELETE, -1L, -1L);
                if (deleteStatus) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, entity.toJson()));
                }
            } else {
                deleteStatus = TodoReference.delete(null, entity.getId());
                if (deleteStatus) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, entity.toJson()));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_DELETE_ALARM, entity.toJson()));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
                }
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            if (ProcessStatus.UN_SYNC_CREATE.equals(entity.getProcessStatus())) {
                boolean deleteStatus = TodoReference.delete(null, entity.getId());
                if (deleteStatus) {
                    entity.setProcessStatus(ProcessStatus.UN_SYNC_DELETE);
                    entity.setStatus(TodoStatus.DELETED);
                    entity.setType(Type.MAIN);
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, entity.toJson()));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_DELETE_ALARM, entity.toJson()));
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
                }
                return;
            }

            ApiManager.doTodoUpdate(context, entity, new TodoUpdateRequest.Listener() {
                @Override
                public void onSuccess(String todoId, long updateTime) {
                    boolean deleteStatus = TodoReference.delete(null, entity.getId());
                    if (deleteStatus) {
                        entity.setProcessStatus(ProcessStatus.UN_SYNC_DELETE);
                        entity.setType(Type.MAIN);
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_TODO_REFRESH, entity.toJson()));
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_TODO_DELETE_ALARM, entity.toJson()));
                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
//                        toast(context, context.getString(R.string.todo_reminder_delete));
                    }
                    getExpiredCount(context);
                }

                @Override
                public void onFailed(String errorMessage) {
                    new AlertView.Builder()
                            .setContext(context)
                            .setStyle(AlertView.Style.Alert)
                            .setTitle(context.getString(R.string.warning_delete_failed))
                            .setMessage(errorMessage)
                            .setCancelText(context.getString(R.string.alert_confirm))
                            .build()
                            .setCancelable(true)
                            .show();
                }
            });
        }
    }

    public static void completeAndSync(Context context, TodoEntity entity, RefreshSource source) {
        entity.setProcessStatus(ProcessStatus.UNDEF.equals(entity.getProcessStatus()) ? ProcessStatus.UN_SYNC_COMPLETE : entity.getProcessStatus());
        entity.setStatus(TodoStatus.DONE);
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            boolean saveStatus = TodoReference.update(null, entity);
            if (saveStatus) {
                TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                if(localEntity != null) {
                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, localEntity.toJson()));
                }
            }
        }
        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doTodoComplete(context, entity, new TodoCompleteRequest.Listener() {
                @Override
                public void onSuccess(String todoId, long updateTime, String status) {
                    entity.setUpdateTime(updateTime);
                    entity.setProcessStatus(ProcessStatus.UNDEF);
                    boolean saveStatus = TodoReference.update(null, entity);
                    if (saveStatus) {
                        TodoEntity localEntity = TodoReference.findById(null, entity.getId());
                        if(localEntity != null) {
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, localEntity.toJson()));
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_DELETE_ALARM, localEntity.toJson()));
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT));
                        }
                    }

                    if (saveStatus) {
                        toast(context, context.getString(R.string.todo_reminder_complete));
                    }

                    getExpiredCount(context);
                }

                @Override
                public void onFailed(String errorMessage) {
                    new AlertView.Builder()
                            .setContext(context)
                            .setStyle(AlertView.Style.Alert)
                            .setTitle(context.getString(R.string.warning_update_failed))
                            .setMessage(errorMessage)
                            .setCancelText(context.getString(R.string.alert_confirm))
                            .build()
                            .setCancelable(true)
                            .show();
                }
            });
        }
    }

    public static void getItem(Context context, String todoId, RefreshSource source, @Nullable ServiceCallBack<TodoEntity, RefreshSource> callBack) {
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            TodoEntity entity = TodoReference.findById(null, todoId);
            if (entity != null && callBack != null) {
                entity.setType(Type.MAIN);
                callBack.complete(entity, RefreshSource.LOCAL);
            } else if (entity == null && callBack != null) {
                callBack.error("query not found ");
            }
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doTodoItem(context, todoId, new ApiListener<TodoEntity>() {
                @Override
                public void onSuccess(TodoEntity entity) {
                    boolean saveStatus = TodoReference.save(null, entity);
                    if (saveStatus) {
                        if (callBack != null) {
                            callBack.complete(entity, RefreshSource.REMOTE);
                        } else {
                            TodoEntity localEntity = TodoReference.findById(null, todoId);
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_UPDATE_ALARM, localEntity.toJson()));
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.UI_NOTICE_TODO_REFRESH, localEntity.toJson()));
                        }
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (callBack != null) {
                        callBack.error(errorMessage);
                    }
                }
            });
        }
    }

    public static void unBindRoom(Context context, String roomId, @Nullable ServiceCallBack<String, RefreshSource> callBack) {
        boolean status = TodoReference.updateRoomId(null, roomId);
        if (status) {
            EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_TODO_UNBIND_ROOM, roomId));
        }
        if (callBack != null) {
            if (status) {
                callBack.complete(roomId, RefreshSource.LOCAL);
            } else {
                callBack.error("remove bind failure , roomId::" + roomId);
            }
        }
    }

    private static synchronized void toast(Context context, String tip) {
        Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
    }
}
