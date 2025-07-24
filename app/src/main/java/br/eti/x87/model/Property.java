package br.eti.x87.model;

public class Property {

    private Long id;
    private String name;
    private String surname;
    private String area;

    public Property(Long id, String name, String surname, String area){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.area = area;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getArea() { return area;  }

    public void setArea(String area) { this.area = area; }
}
