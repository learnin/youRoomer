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

public class Attachment implements Serializable {

	private String originalFilename;
	private String contentType;
	private String attachmentType;
	private String filename;

	// FIXME 型確認必要 private Object data;

	/**
	 * originalFilenameを取得します。
	 *
	 * @return originalFilename
	 */
	public String getOriginalFilename() {
		return originalFilename;
	}

	/**
	 * originalFilenameを設定します。
	 *
	 * @param originalFilename originalFilename
	 */
	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	/**
	 * contentTypeを取得します。
	 *
	 * @return contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * contentTypeを設定します。
	 *
	 * @param contentType contentType
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * attachmentTypeを取得します。
	 *
	 * @return attachmentType
	 */
	public String getAttachmentType() {
		return attachmentType;
	}

	/**
	 * attachmentTypeを設定します。
	 *
	 * @param attachmentType attachmentType
	 */
	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	/**
	 * filenameを取得します。
	 *
	 * @return filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * filenameを設定します。
	 *
	 * @param filename filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
