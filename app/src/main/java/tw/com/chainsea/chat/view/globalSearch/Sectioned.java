package tw.com.chainsea.chat.view.globalSearch;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * current by evan on 2019-12-25
 */
public class Sectioned<T, E extends Enum, B> {
    private E type;
    private String name;
    @StringRes
    private int nameResId;
    private String content;
    private List<T> datas = Lists.newArrayList();
    private int size;
    private boolean isOpen;
    private Object tag;
    private String serviceNumberId;
    private B bind;
    private boolean hidingSectioned = false;
    private boolean hasFooter = false;

    Sectioned(E type, String name, int nameResId, String content, List<T> datas, int size, boolean isOpen, Object tag, String serviceNumberId, B bind, boolean hidingSectioned, boolean hasFooter) {
        this.type = type;
        this.name = name;
        this.nameResId = nameResId;
        this.content = content;
        this.datas = datas;
        this.size = size;
        this.isOpen = isOpen;
        this.tag = tag;
        this.serviceNumberId = serviceNumberId;
        this.bind = bind;
        this.hidingSectioned = hidingSectioned;
        this.hasFooter = hasFooter;
    }

    private static List $default$datas() {
        return Lists.newArrayList();
    }

    private static boolean $default$hidingSectioned() {
        return false;
    }

    private static boolean $default$hasFooter() {
        return false;
    }

    public static <T, E extends Enum, B> SectionedBuilder<T, E, B> Build() {
        return new SectionedBuilder<T, E, B>();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? nameResId : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Sectioned other = (Sectioned) obj;

        // Using a single boolean result variable
        boolean isEqual;

        if (this.name == null) {
            if (other.getName() == null) {
                // Both names are null, compare by resource IDs
                isEqual = (this.nameResId != 0 || other.getNameResId() != 0) &&
                    (this.nameResId == other.getNameResId());
            } else {
                // This name is null but other's isn't
                isEqual = false;
            }
        } else {
            // This name is not null, compare with other's name
            isEqual = this.name.equals(other.getName());
        }

        return isEqual;
    }

    public E getType() {
        return type;
    }

    public void setType(E type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNameResId() {
        return nameResId;
    }

    public void setNameResId(int nameResId) {
        this.nameResId = nameResId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public B getBind() {
        return bind;
    }

    public void setBind(B bind) {
        this.bind = bind;
    }

    public boolean isHidingSectioned() {
        return hidingSectioned;
    }

    public void setHidingSectioned(boolean hidingSectioned) {
        this.hidingSectioned = hidingSectioned;
    }

    public boolean isHasFooter() {
        return hasFooter;
    }

    public void setHasFooter(boolean hasFooter) {
        this.hasFooter = hasFooter;
    }

    public String getServiceNumberId() {
        return serviceNumberId;
    }

    public SectionedBuilder<T, E, B> toBuilder() {
        return new SectionedBuilder<T, E, B>().type(this.type).name(this.name).nameResId(this.nameResId).content(this.content).datas(this.datas).size(this.size).isOpen(this.isOpen).tag(this.tag).serviceNumberId(this.serviceNumberId).bind(this.bind).hidingSectioned(this.hidingSectioned).hasFooter(this.hasFooter);
    }

    public static class SectionedBuilder<T, E extends Enum, B> {
        private E type;
        private String name;
        private int nameResId;
        private String content;
        private List<T> datas$value;
        private boolean datas$set;
        private int size;
        private boolean isOpen;
        private Object tag;
        private String serviceNumberId;
        private B bind;
        private boolean hidingSectioned$value;
        private boolean hidingSectioned$set;
        private boolean hasFooter$value;
        private boolean hasFooter$set;

        SectionedBuilder() {
        }

        public SectionedBuilder<T, E, B> type(E type) {
            this.type = type;
            return this;
        }

        public SectionedBuilder<T, E, B> name(String name) {
            this.name = name;
            return this;
        }

        public SectionedBuilder<T, E, B> nameResId(int nameResId) {
            this.nameResId = nameResId;
            return this;
        }

        public SectionedBuilder<T, E, B> content(String content) {
            this.content = content;
            return this;
        }

        public SectionedBuilder<T, E, B> datas(List<T> datas) {
            this.datas$value = datas;
            this.datas$set = true;
            return this;
        }

        public SectionedBuilder<T, E, B> size(int size) {
            this.size = size;
            return this;
        }

        public SectionedBuilder<T, E, B> isOpen(boolean isOpen) {
            this.isOpen = isOpen;
            return this;
        }

        public SectionedBuilder<T, E, B> tag(Object tag) {
            this.tag = tag;
            return this;
        }

        public SectionedBuilder<T, E, B> serviceNumberId(String serviceNumberId) {
            this.serviceNumberId = serviceNumberId;
            return this;
        }

        public SectionedBuilder<T, E, B> bind(B bind) {
            this.bind = bind;
            return this;
        }

        public SectionedBuilder<T, E, B> hidingSectioned(boolean hidingSectioned) {
            this.hidingSectioned$value = hidingSectioned;
            this.hidingSectioned$set = true;
            return this;
        }

        public SectionedBuilder<T, E, B> hasFooter(boolean hasFooter) {
            this.hasFooter$value = hasFooter;
            this.hasFooter$set = true;
            return this;
        }

        public Sectioned<T, E, B> build() {
            List<T> datas$value = this.datas$value;
            if (!this.datas$set) {
                datas$value = Sectioned.$default$datas();
            }
            boolean hidingSectioned$value = this.hidingSectioned$value;
            if (!this.hidingSectioned$set) {
                hidingSectioned$value = Sectioned.$default$hidingSectioned();
            }
            boolean hasFooter$value = this.hasFooter$value;
            if (!this.hasFooter$set) {
                hasFooter$value = Sectioned.$default$hasFooter();
            }
            return new Sectioned<T, E, B>(type, name, nameResId, content, datas$value, size, isOpen, tag, serviceNumberId, bind, hidingSectioned$value, hasFooter$value);
        }

        @NonNull
        public String toString() {
            return "Sectioned.SectionedBuilder(type=" + this.type + ", name=" + this.name + ", nameResId=" + this.nameResId + ", content=" + this.content + ", datas$value=" + this.datas$value + ", size=" + this.size + ", isOpen=" + this.isOpen + ", tag=" + this.tag + ", serviceNumberId=" + this.serviceNumberId + ", bind=" + this.bind + ", hidingSectioned$value=" + this.hidingSectioned$value + ", hasFooter$value=" + this.hasFooter$value + ")";
        }
    }
}
