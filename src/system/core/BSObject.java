package system.core;

/**
 * Базовый класс объектов в системе.
 */
public class BSObject {
    /**
     * Статус активной записи в системе
     */
    public static final int STATUS_ACTIVE = 10;
    /**
     * Статус удалённой записи в системе
     */
    public static final int STATUS_DELETE = 70;

    /**
     * Дата создания записи
     */
    private long createDate;
    /**
     * Идентификатор пользователя, являющийся инициатором создания записи
     */
    private int creator;
    /**
     * Статус записи (10 - запись активна, 70 - запись удалена)
     */
    private int status;
    /**
     * Идентфикиатор записи
     */
    private int ouid;
    /**
     * Глобальный уникальный идентификатор записи
     */
    private String guid;

    public long getCreateDate () {
        return createDate;
    }

    public void setCreateDate (long createDate) {
        this.createDate = createDate;
    }

    public int getCreator () {
        return creator;
    }

    public void setCreator (int creator) {
        this.creator = creator;
    }

    public int getStatus () {
        return status;
    }

    public void setStatus (int status) {
        this.status = status;
    }

    public int getOuid () {
        return ouid;
    }

    public void setOuid (int ouid) {
        this.ouid = ouid;
    }

    public String getGuid () {
        return guid;
    }

    public void setGuid (String guid) {
        this.guid = guid;
    }
}
