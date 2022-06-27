package main.dto;


import lombok.Getter;

import java.util.ArrayList;
public class LinkNodeDto {
    private volatile LinkNodeDto parent;
    @Getter
    private final String url;
    private final ArrayList<LinkNodeDto> children;

    public LinkNodeDto(String url) {
        this.parent = null;
        this.url = url;
        this.children = new ArrayList<>();
    }
    public synchronized void addChild(LinkNodeDto child) {
        LinkNodeDto root = getRootElement();
        if (!root.contains(child.getUrl())) {
            child.setParent(this);
            children.add(child);
        }
    }
    private void setParent(LinkNodeDto linkNodeDto) {
        synchronized (this) {
            this.parent = linkNodeDto;
        }
    }
    public LinkNodeDto getRootElement() {
        return parent == null ? this : parent.getRootElement();
    }
    private boolean contains(String url) {
        if (this.url.equals(url)) {
            return true;
        }
        for (LinkNodeDto child : children) {
            if (child.contains(url))
                return true;
        }
        return false;
    }
    public ArrayList<LinkNodeDto> getChildren() {
        return children;
    }
}
