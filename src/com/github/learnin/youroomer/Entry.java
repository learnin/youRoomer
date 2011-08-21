package com.github.learnin.youroomer;

import java.util.Date;
import java.util.List;

public class Entry {

	private long id;
	private String content;
	private Date createdAt;
	private Date updatedAt;
	private Participation participation;
	private List<Entry> children;

	/**
	 * idを取得します。
	 * @return id
	 */
	public long getId() {
	    return id;
	}

	/**
	 * idを設定します。
	 * @param id id
	 */
	public void setId(long id) {
	    this.id = id;
	}

	/**
	 * contentを取得します。
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * contentを設定します。
	 * @param content content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * createdAtを取得します。
	 * @return createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * createdAtを設定します。
	 * @param createdAt createdAt
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * updatedAtを取得します。
	 * @return updatedAt
	 */
	public Date getUpdatedAt() {
	    return updatedAt;
	}

	/**
	 * updatedAtを設定します。
	 * @param updatedAt updatedAt
	 */
	public void setUpdatedAt(Date updatedAt) {
	    this.updatedAt = updatedAt;
	}

	/**
	 * participationを取得します。
	 * @return participation
	 */
	public Participation getParticipation() {
	    return participation;
	}

	/**
	 * participationを設定します。
	 * @param participation participation
	 */
	public void setParticipation(Participation participation) {
	    this.participation = participation;
	}

	/**
	 * childrenを取得します。
	 * @return children
	 */
	public List<Entry> getChildren() {
	    return children;
	}

	/**
	 * childrenを設定します。
	 * @param children children
	 */
	public void setChildren(List<Entry> children) {
	    this.children = children;
	}

}
