public class PersonTwo {
  private String name;
  private String surname;
  
  public PersonTwo(String name,  String surname){
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

