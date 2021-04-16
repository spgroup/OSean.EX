package org;
public class Person {
  private String name;
  private String surname;
  public Person(  String name,  String surname){
    this.name=name;
    this.surname=surname;
  }
  public String getName(){
    return this.name;
  }
  public String getFullName(){
    return name + " " + surname;
  }
}
