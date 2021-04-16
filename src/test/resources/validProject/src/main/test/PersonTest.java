import org.junit.Assert;
import org.junit.Test;

public class PersonTest {

  @Test
  public void testName() {
    Person person = new Person("Maria", "da Silva");
    Assert.assertEquals("Maria", person.getName());
  }

  @Test
  public void testFullName() {
    Person person = new Person("Maria", "da Silva");
    Assert.assertEquals("Maria da Silva", person.getFullName());
  }

}