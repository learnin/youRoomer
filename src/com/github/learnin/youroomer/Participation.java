package com.github.learnin.youroomer;

public class Participation {

	private long id;
	private String name;
	private Group group;

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
	 * groupを取得します。
	 *
	 * @return group
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * groupを設定します。
	 *
	 * @param group group
	 */
	public void setGroup(Group group) {
		this.group = group;
	}
}
