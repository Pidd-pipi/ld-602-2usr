package com.generated.rescueStock.models;

public class Warehouse {
  public long id;
  public String name;
  public String district;
  public String address;
  public long managerId;
  public int capacityLevel;
  public String contactPhone;
  public String status;

  public Warehouse() {}

  public Warehouse(long id, String name, String district, String address, long managerId, int capacityLevel,
      String contactPhone, String status) {
    this.id = id;
    this.name = name;
    this.district = district;
    this.address = address;
    this.managerId = managerId;
    this.capacityLevel = capacityLevel;
    this.contactPhone = contactPhone;
    this.status = status;
  }
}
