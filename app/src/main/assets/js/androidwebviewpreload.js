//这行是解决输入框在输入信息弹出键盘后页面变形
window.onload = function () { $('body').css({'height':$(window).height()})};
//微信内置浏览器浏览H5页面弹出的键盘遮盖文本框的解决办法
window.addEventListener("resize", function () {
  if (document.activeElement.tagName == "INPUT" || document.activeElement.tagName == "TEXTAREA") {
    window.setTimeout(function () {
      document.activeElement.scrollIntoViewIfNeeded();
    }, 0);
  }
})

var EventEmitter = {
  events: {},
  on(type, listener, isUnshift) {
    // 因为其他的类可能继承自EventEmitter，子类的events可能为空，保证子类必须存在此实例属性
    if(!EventEmitter.events) {
      EventEmitter.events = {};
    }
    if(EventEmitter.events[type]) {
      if(isUnshift) {
        EventEmitter.events[type].unshift(listener);
      } else {
        EventEmitter.events[type].push(listener);
      }
    } else {
      EventEmitter.events[type] = [listener]
    }

  },
  emit(type, ...args) {
    if(EventEmitter.events[type]) {
      EventEmitter.events[type].forEach(fn => fn.call(EventEmitter, ...args));
    }
  },
  // 只绑定一次，然后解绑
  once(type, listener) {
    function oneTime(...args) {
      listener.call(EventEmitter, ...args);
      EventEmitter.off(type, oneTime);
    }
    EventEmitter.on(type, oneTime)
  },
  off(type, listener) {
    if(EventEmitter.events[type]) {
      const index = EventEmitter.events[type].indexOf(listener);
      EventEmitter.events[type].splice(index, 1);
    }
  },
  removeAllListen(){
    EventEmitter.events = {};
  }
}

var AileClientAPI = {
  init: function (apId) {
    // init 用于传递apid ，由ceclient 根据apid认证并返回openId 以及账户相关信息。
    // 若openid存在
    window.jsToAndroid.onCallBack(JSON.stringify({'apid': apId, 'channel': 'init'}));
  },

  callBack: function (channel, data) {
    var newData = JSON.parse(data);
    EventEmitter.emit(channel, channel, newData);
  },

  removeAllListen: function () {
    EventEmitter.removeAllListen();
  },

  listen: function (channel, callback) {
    EventEmitter.on(channel, callback, false)
  },

  listenonce: function (channel, callback) {
    EventEmitter.on(channel, callback,false)
  },

  getAvatar: function (openid, type) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'openId': openid, 'type': type, 'channel': 'getAvatar' }));
  },

  closeAiff: function () {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'closeAiff' }));
  },

  openRoom: function (roomId, key) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'openRoom', 'roomId': roomId, 'key': key }));
  },

  openUserRoom: function (openId, key) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'openUserRoom', 'openId': openId, 'key': key }));
  },

  getOpener: function () {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'getOpener' }));
  },

  sendNumber: function (num) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'sendNumber', 'num': num }));
  },

  sendState: function (ib) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'sendState', "ib": ib }));
  },

  sendMessage: function (msg, type) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'sendMessage', 'msg': msg, 'type': type }));
  },

  selectMessage: function (msgObj) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'selectMessage', 'msg': msgObj }));
  },

  getUser: function (openId, type) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'getUser', 'openId': openId, 'type': type }));
  },

  getAllRooms: function (key) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'getAllRooms', "key": key }));
  },

  getUserRooms: function (openId, key) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'getUserRooms', 'openId': openId, "key": key }));
  },

  getCurrentPosition: function(identifier) {
    window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'getCurrentPosition', 'identifier': identifier}));
  },

  subscribeCurrentPosition: function(identifier) {
      window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'subscribeCurrentPosition', 'identifier': identifier}));
    },

  openDevTools: function () {

  },

  navigateToTab: function (id, args) {

  },

  openWindow: function (args) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'openWindow', 'args': args }));
  },

  callRoom: function (roomId) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'callRoom', 'roomId': roomId }));
  },

  callContactRoom: function (userId) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'callContactRoom', 'userId': userId }));
  },

  openContact: function (userId) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'openContact', 'userId': userId }));
  },

  openContactChat: function (serviceNumberId, openId) {
    window.jsToAndroid.onCallBack(JSON.stringify({ 'channel': 'openContactChat', 'serviceNumberId': serviceNumberId, 'openId': openId }));
  },

  openBossContactChat: function (openId) {
    window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'openBossContactChat', 'openId': openId}));
  },

  getRoomStatus: function (roomId) {
    window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'getRoomStatus', 'roomId': roomId}));
  },

  shareTargetPicker: function (templateObj, type, key) {
    window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'shareTargetPicker', 'templateObj': content, 'type': type, 'key': key}));
  },

  sendState: function (ibState) {
    window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'sendState', 'ibState': ibState}));
  },

  sendNumber: function (iNumber) {
    window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'sendNumber', 'iNumber': iNumber}));
  },

  getAiffItem: function (aiffKey, key) {
    window.jsToAndroid.onCallBack(JSON.stringify({'channel': 'getAiffItem', 'aiffKey': aiffKey, 'key': key}));
  },
};

window["AileAPI"] = AileClientAPI;

var LocalAiffJS = {
  version: "1.0.1",
  onCloseWindow: function (cancelFun) {
    cancelFun(false);
  },
  _initEvent: function () {
    if (window["AileAPI"]) {
      window["AileAPI"].removeAllListen();
      window["AileAPI"].listen("closeAiff", (c, d) => {
        LocalAiffJS.onCloseWindow(LocalAiffJS._closeEvent);
      });
    }
  },
  _closeEvent: function (cancel) {
    if (!cancel) {
      window["AileAPI"].closeAiff();
    }
  },
  openDev: function () {
    if (window["AileAPI"]) {
      window["AileAPI"].openDevTools();
    }
  },
};

LocalAiffJS._initEvent();
