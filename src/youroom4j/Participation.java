/*
 * Copyright 2012 Manabu Inoue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package youroom4j;

import java.io.Serializable;


public class Participation implements Serializable {

	private static final long serialVersionUID = 1L;
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

	public String getUserImageURI() {
		return "https://www.youroom.in/r/" + group.getToParam() + "/participations/" + id + "/picture";
	}
}
