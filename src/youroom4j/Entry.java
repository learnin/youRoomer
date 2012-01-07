package youroom4j;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Entry implements Serializable {

	private static final long serialVersionUID = 1L;
	private long id;
	private String content;
	private Date createdAt;
	private Date updatedAt;
	private boolean canUpdate;
	private boolean hasRead;
	private int level;
	private long rootId;
	private Participation participation;
	private Attachment attachment;

	// コメントの総数(ルートエントリ以外はおそらく常に0)
	private int descendantsCount = 0;

	private List<Entry> children;
	private long parentId;

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
	 * updatableを取得します。
	 *
	 * @return canUpdate
	 */
	public boolean isUpdatable() {
		return canUpdate;
	}

	/**
	 * updatableを設定します。
	 *
	 * @param updatable updatable
	 */
	public void setUpdatable(boolean updatable) {
		this.canUpdate = updatable;
	}

	/**
	 * hasReadを取得します。
	 *
	 * @return hasRead
	 */
	public boolean hasRead() {
		return hasRead;
	}

	/**
	 * hasReadを設定します。
	 *
	 * @param hasRead hasRead
	 */
	public void setHasRead(boolean hasRead) {
		this.hasRead = hasRead;
	}

	/**
	 * levelを取得します。
	 *
	 * @return level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * levelを設定します。
	 *
	 * @param level level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * rootIdを取得します。
	 *
	 * @return rootId
	 */
	public long getRootId() {
		return rootId;
	}

	/**
	 * rootIdを設定します。
	 *
	 * @param rootId rootId
	 */
	public void setRootId(long rootId) {
		this.rootId = rootId;
	}

	/**
	 * participationを取得します。
	 *
	 * @return participation
	 */
	public Participation getParticipation() {
		return participation;
	}

	/**
	 * participationを設定します。
	 *
	 * @param participation participation
	 */
	public void setParticipation(Participation participation) {
		this.participation = participation;
	}

	/**
	 * attachmentを取得します。
	 *
	 * @return attachment
	 */
	public Attachment getAttachment() {
		return attachment;
	}

	/**
	 * attachmentを設定します。
	 *
	 * @param attachment attachment
	 */
	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	/**
	 * descendantsCountを取得します。
	 * @return descendantsCount
	 */
	public int getDescendantsCount() {
	    return descendantsCount;
	}

	/**
	 * descendantsCountを設定します。
	 * @param descendantsCount descendantsCount
	 */
	public void setDescendantsCount(int descendantsCount) {
	    this.descendantsCount = descendantsCount;
	}

	/**
	 * childrenを取得します。
	 *
	 * @return children
	 */
	public List<Entry> getChildren() {
		return children;
	}

	/**
	 * childrenを設定します。
	 *
	 * @param children children
	 */
	public void setChildren(List<Entry> children) {
		this.children = children;
	}

	/**
	 * parentIdを取得します。
	 *
	 * @return parentId
	 */
	public long getParentId() {
		return parentId;
	}

	/**
	 * parentIdを設定します。
	 *
	 * @param long parentId
	 */
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

}
