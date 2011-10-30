package com.github.learnin.youroomer;

import java.io.Serializable;
import java.util.Date;

/**
 * グループ(ルーム)
 *
 * @author learn
 */
public class Group implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private Date createdAt;
	private String name;
	private boolean opened;
	private Date updatedAt;
	private String toParam;

	/**
	 * idを取得します。
	 *
	 * @return id
	 */
	public long getId() {
		return id;
	}

	/**
	 * idを設定します。
	 *
	 * @param id id
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * createdAtを取得します。
	 *
	 * @return createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * createdAtを設定します。
	 *
	 * @param createdAt createdAt
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * nameを取得します。
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * nameを設定します。
	 *
	 * @param name name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * openedを取得します。
	 *
	 * @return opened
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * openedを設定します。
	 *
	 * @param opened opened
	 */
	public void setOpened(boolean opened) {
		this.opened = opened;
	}

	/**
	 * updatedAtを取得します。
	 *
	 * @return updatedAt
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * updatedAtを設定します。
	 *
	 * @param updatedAt updatedAt
	 */
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * toParamを取得します。
	 *
	 * @return toParam
	 */
	public String getToParam() {
		return toParam;
	}

	/**
	 * toParamを設定します。
	 *
	 * @param toParam toParam
	 */
	public void setToParam(String toParam) {
		this.toParam = toParam;
	}
}
