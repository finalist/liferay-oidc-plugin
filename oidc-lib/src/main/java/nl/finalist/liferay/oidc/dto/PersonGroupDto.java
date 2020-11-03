package nl.finalist.liferay.oidc.dto;

public class PersonGroupDto {

    private final String uuid;
    private final String name;

    private PersonGroupDto(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public static PersonGroupDto of(String uuid, String name) {
        return new PersonGroupDto(uuid, name);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

}
