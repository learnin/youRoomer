package com.github.learnin.youroomer;

import com.github.learnin.youroomer.R;
import java.util.Date;

public class Entry {

	private String content;
	private Date created;
	private String userName;
	private String groupName;

	/**
	 * contentを取得します。
	 *
	 * @return content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * contentを設定します。
	 *
	 * @param content content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * createdを取得します。
	 *
	 * @return created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * createdを設定します。
	 *
	 * @param created created
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * userNameを取得します。
	 *
	 * @return userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * userNameを設定します。
	 *
	 * @param userName userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * groupNameを取得します。
	 *
	 * @return groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * groupNameを設定します。
	 *
	 * @param groupName groupName
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
}
