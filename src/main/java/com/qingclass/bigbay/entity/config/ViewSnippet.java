package com.qingclass.bigbay.entity.config;

import java.util.Date;

public class ViewSnippet {
	private long id;
	private String snippetKey;
	private String content;
	private Date createdAt;
	private Date updatedAt;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getSnippetKey() {
		return snippetKey;
	}
	public void setSnippetKey(String snippetKey) {
		this.snippetKey = snippetKey;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	
}
