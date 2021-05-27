package ru.netology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import ru.netology.domain.UserGenerator;

import java.time.Duration;
import java.util.*;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppCardDeliveryTest {
    Date date = new Date();
    Calendar cal = Calendar.getInstance();
    UserGenerator.User user = UserGenerator.Registration.generateUser("ru");

    @BeforeEach
    public void setUpDate() {
        cal.setTime(date);
        open("http://localhost:9999");
    }

    public void setDate(int shift) {
        //this hack is here because .clear() doesn't work :(
        $("[data-test-id='date'] .input__control").sendKeys(Keys.CONTROL + "A");
        $("[data-test-id='date'] .input__control").sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] .input__control").setValue(UserGenerator.generateDate(shift));
    }

    public void fillIn(String... args) {
        for (String arg : args) {
            switch (arg) {
                case "name":
                    $("[data-test-id='name'] .input__control").setValue(user.getName());
                    break;
                case "city":
                    $("[data-test-id='city'] .input__control").sendKeys(user.getCity());
                    break;
                case "phone":
                    $("[data-test-id='phone'] .input__control").setValue(user.getPhone());
                    break;
                case "agreement":
                    $("[data-test-id='agreement']").click();
                    break;
                case "button":
                    $(".button").click();
            }
        }
    }

    @Test
    public void positiveTest() {
        setDate(5);
        fillIn("name", "phone", "agreement", "city", "button");
        $(withText("Успешо!")).shouldBe(visible, Duration.ofSeconds(15));
    }

    @Test
    public void negativeCityOutOfBoundsTest() {
        $("[data-test-id='city'] .input__control").sendKeys("Корсаков");
        setDate(5);
        fillIn("name", "phone", "agreement", "button");
        $("[data-test-id='city'].input_invalid").shouldBe(visible).shouldHave(exactText("Доставк в выбранный город недоступна"));
    }

    @Test
    public void negativeNonRussianCityTest() {
        $("[data-test-id='city'] .input__control").sendKeys(UserGenerator.generateCity("en"));
        setDate(5);
        fillIn("name", "phone", "agreement", "button");
        $("[data-test-id='city'].input_invalid").shouldBe(visible).shouldHave(exactText("Доставк в выбранный город недоступна"));
    }

    @Test
    public void negativeNoCityTest() {
        setDate(5);
        fillIn("name", "phone", "agreement", "button");
        $("[data-test-id='city'].input_invalid .input__sub").shouldBe(visible).shouldHave(exactText("Пол обязательно для заполнения"));
    }

    @Test
    public void negativeNonRussianNameTest() {
        $("[data-test-id='name'] .input__control").setValue(UserGenerator.generateName("en"));
        setDate(5);
        fillIn("phone", "agreement", "city", "button");
        $("[data-test-id='name'].input_invalid .input__sub").shouldBe(visible).shouldHave(exactText("Им и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test @Disabled
    public void yoNameTest() {
        $("[data-test-id='name'] .input__control").setValue("Алёна");
        setDate(5);
        fillIn("phone", "agreement", "city", "button");
        $(withText("Успешо!")).shouldBe(visible, Duration.ofSeconds(15));
    }

    @Test
    public void negativeNonAlphaNameTest() {
        $("[data-test-id='name'] .input__control").setValue("А! овар,.");
        setDate(5);
        fillIn("phone", "agreement", "city", "button");
        $("[data-test-id='name'].input_invalid .input__sub").shouldBe(visible).shouldHave(exactText("Им и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы."));
    }

    @Test
    public void negativeNoNameTest() {
        setDate(5);
        fillIn("phone", "agreement", "city", "button");
        $("[data-test-id='name'].input_invalid .input__sub").shouldBe(visible).shouldHave(exactText("Пол обязательно для заполнения"));
    }

    @Test @Disabled
    public void negativeAnyPhoneTest() {
        $("[data-test-id='phone'] .input__control").setValue("000");
        setDate(5);
        fillIn("name", "agreement", "city", "button");
        $("[data-test-id='phone'].input_invalid .input__sub").shouldBe(visible);
    }

    @Test
    public void negativeNonNumPhoneTest() {
        $("[data-test-id='phone'] .input__control").setValue("lksajdahfdf");
        setDate(5);
        fillIn("name", "agreement", "city", "button");
        $("[data-test-id='phone'] .input__control").shouldHave(attribute("value", "+"));
    }

    @Test
    public void negativeNoPhoneTest() {
        setDate(5);
        fillIn("name", "agreement", "city", "button");
        $("[data-test-id='phone'].input_invalid .input__sub").shouldBe(visible).shouldHave(exactText("Пол обязательно для заполнения"));
    }

    @Test
    public void autofillTest() {
        $("[data-test-id='city'] .input__control").sendKeys("Мо");
        $$(".menu-item .menu-item__control").find(exactText("Москва")).click();
        $("[data-test-id='city'] .input__control").shouldHave(attribute("value", "Москва"));
        setDate(5);
        fillIn("name", "phone", "agreement", "button");
        $(withText("Успешо!")).shouldBe(visible, Duration.ofSeconds(15));
    }

    @Test
    public void calendarWidgetTest() {
        $("[data-test-id='date'] .input__control").click();

        Calendar newCal = Calendar.getInstance();//get a new calendar to track the new date
        newCal.add(newCal.DATE, 7);
        String newDate = newCal.get(newCal.DAY_OF_MONTH) + "";
        if (cal.get(cal.MONTH) != newCal.get(newCal.MONTH)) {//click the right arrow if the new and old months don't match
            $$(".calendar__arrow_direction_right").last().click();
        }

        //set up the check of the new month's number
        $$(".calendar__row .calendar__day[data-day]").find(exactText(newDate)).click();//set the new date
        String actual = $("[data-test-id='date'] .input__control").getValue().substring(3, 5);//get the numeric value of the new month
        int temp = newCal.get(newCal.MONTH) + 1; //get the numeric value of the expected month (zero-indexed)
        String expected = temp + ""; //convert it to string
        if (temp < 10) {//pad with a zero if September or earlier
            expected = "0" + expected;
        }

        $(".button").click();
        fillIn("name", "phone", "agreement", "city", "button");
        $(withText("Успешо!")).shouldBe(visible, Duration.ofSeconds(15));
        assertEquals(actual, expected);
    }

    @Test
    public void rescheduleTest() {
        fillIn("city", "name", "phone", "agreement", "button");
        setDate(7);
        $(".button").click();
        $(".button.button_size_s").shouldHave(exactText("Перепланировать")).shouldBe(visible, Duration.ofSeconds(7)).click();
        $(withText("Успешо!")).shouldBe(visible, Duration.ofSeconds(15));
    }
}