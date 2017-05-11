package gov.sba.automation;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.base.Function;

// import gov.sba.utils.integration.LoginPageWithReference;

public class CommonApplicationMethods {

	public static void display(String smeThng) throws Exception {
		LogManager.getLogger(gov.sba.automation.CommonApplicationMethods.class.getName()).info(smeThng);
	}

	public static Map getLocator(String locatorName) throws YamlException, FileNotFoundException {
		YamlReader reader = new YamlReader(new FileReader(FixtureUtils.fixturesDir() + "Locators.yaml"));
		Object object = reader.read(); // System.out.println(object);
		Map map = (Map) object; // System.out.println(map.get(locatorName));
		return (Map) map.get(locatorName);
	};

	public static void take_ScreenShot_TestCaseName(WebDriver webDriver, String[] stringValueArray) throws Exception {
		File src = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
		String time = Integer.toString((int) (new Date().getTime() / 1000));
		display(time);

		try {
			// now copy the screenshot to the screenshot folder.
			if (stringValueArray.length == 2) {
				FileUtils.copyFile(src, new File(
						FixtureUtils.get_SS_Dir() + stringValueArray[0] + stringValueArray[1] + time + ".png"));
			} else {
				FileUtils.copyFile(src,
						new File(FixtureUtils.get_SS_Dir() + stringValueArray[0] + "Exception" + ".png"));
			}
		}

		catch (IOException e) {
			throw e;
		}

	};

	public static void take_Desktop_SShot_TestCaseName(String[] stringValueArray) throws Exception {
		Robot robot = new Robot();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd hh mm ss a");
		Calendar now = Calendar.getInstance();
		BufferedImage screenShot = robot
				.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		if (stringValueArray.length == 2) {
			ImageIO.write(screenShot, "JPG",
					new File(FixtureUtils.get_SS_Dir() + stringValueArray[0] + stringValueArray[1] + ".jpg"));
		} else {
			ImageIO.write(screenShot, "JPG",
					new File(FixtureUtils.get_SS_Dir() + stringValueArray[0] + "_Exception" + ".jpg"));
		}

	};

	public static Boolean checkApplicationExists(WebDriver webDriver, String type_Of_App, String status_Of_App)
			throws Exception {
		// It should be in Vendor Dashboard
		switch (type_Of_App.toLowerCase((Locale.ENGLISH)) + status_Of_App.toLowerCase((Locale.ENGLISH))) {
		case "edwosbactive":
			List<WebElement> listOfActiveEDWOSB = webDriver.findElements(By.xpath("//table[@id='certifications']/tbody/"
					+ "tr[  " + "		(td[position()=5 and contains(text(),'ctive')]) "
					+ "and  (td[position()=1]/a[contains(text(),'EDWOSB')]) " + "	]"));
			return listOfActiveEDWOSB.size() > 0;
		case "wosbactive":
			List<WebElement> listOfActiveWOSB = webDriver.findElements(By.xpath(
					"//table[@id='certifications']/tbody/tr[  " + "(td[position()=5 and contains(text(),'ctive')]) and "
							+ "(td[position()=1]/a[contains(text(),'WOSB') and not(contains(text(),'EDWOSB'))]) ]"));
			return listOfActiveWOSB.size() > 0;
		case "mpppending":
			List<WebElement> listOfActiveMpp = webDriver.findElements(By.xpath(
					"//table[@id='certifications']/tbody/tr[  (td[position()=5 and contains(text(),'ending')]) and (td/a[position()=1 and contains(text(),'MPP')]) ]"));
			return listOfActiveMpp.size() > 0;

		default:
			return false;
		}
	}

	public static List<WebElement> find_Elements_Loc_InPrg(WebDriver webdriver, String type_Locator,
			String value_Locator) throws Exception {
		List<WebElement> element_01 = null;
		for (int i = 0; i < 10; i++) {
			try {
				switch (type_Locator.toLowerCase()) {
				case "xpath":
					element_01 = webdriver.findElements(By.xpath(value_Locator));
				case "id":
					element_01 = webdriver.findElements(By.id(value_Locator));
				case "classname":
					element_01 = webdriver.findElements(By.className(value_Locator));
				case "name":
					element_01 = webdriver.findElements(By.name(value_Locator));
				case "cssselector":
					element_01 = webdriver.findElements(By.cssSelector(value_Locator));
				case "linktext":
					element_01 = webdriver.findElements(By.linkText(value_Locator));
				}

				if (element_01.size() > 0) {
					break;
				}

			} catch (Exception e) {
				display("Trying to find BY " + type_Locator + ":" + value_Locator);
				Thread.sleep(100); // DEEPA: is needed here since we are
									// Repeatedly Finding
			}
		}
		return element_01;
	};

	public static List<WebElement> find_Elements(WebDriver webdriver, String locator_Yaml) throws Exception {
		Map locator = getLocator(locator_Yaml);
		return find_Elements_Loc_InPrg(webdriver, locator.get("Locator").toString(), locator.get("Value").toString());
	}

	public static WebElement find_Element_Loc(WebDriver webdriver, String type_Locator, String value_Locator)
			throws Exception {

		Wait<WebDriver> wait = new FluentWait<WebDriver>(webdriver).withTimeout(7, TimeUnit.SECONDS)
				.pollingEvery(100, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		try {
			switch (type_Locator.toLowerCase()) {
			case "xpath":
				WebElement element_01 = wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver webDriver) {
						return webDriver.findElement(By.xpath(value_Locator));
					}
				});
				display("Hey this came in Xpa");
				return element_01;
			case "id":
				element_01 = wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver webDriver) {
						return webDriver.findElement(By.id(value_Locator));
					}
				});
				display("Hey this came in ID");
				return element_01;
			case "classname":
				element_01 = wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver webDriver) {
						return webdriver.findElement(By.className(value_Locator));
					}
				});
				display("Hey this came in CName");
				return element_01;
			case "name":
				element_01 = wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver webDriver) {
						return webdriver.findElement(By.name(value_Locator));
					}
				});
				display("Hey this came in Name");
				return element_01;
			case "cssselector":
				element_01 = wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver webDriver) {
						return webdriver.findElement(By.cssSelector(value_Locator));
					}
				});
				display("Hey this came in Css");
				return element_01;
			case "linktext":
				element_01 = wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver webDriver) {
						return webdriver.findElement(By.linkText(value_Locator));
					}
				});
				display("Hey this came in Lt");
				return element_01;
			}
		} catch (Exception e) {
			display("Trying to find BY " + type_Locator + ":" + value_Locator);
			throw new Exception("Tried to find BY " + type_Locator + ":" + value_Locator);
		}
		return null;
	};

	public static WebElement find_Element(WebDriver webdriver, String locator_Yaml) throws Exception {
		Map locator = getLocator(locator_Yaml);
		return find_Element_Loc(webdriver, locator.get("Locator").toString(), locator.get("Value").toString());
	}

	public static void click_Element_Loc(WebDriver webdriver, String type_Locator, String value_Locator)
			throws Exception {
		find_Element_Loc(webdriver, type_Locator, value_Locator).click();
	}

	public static void accept_Alert(WebDriver webDriver) throws Exception {
		for (int i = 0; i < 15; i++) {
			try {
				webDriver.switchTo().alert().accept();
				return;
			} catch (Exception e) {
				if (i == 14) {
					throw new Exception("Alert Not found");
				} else {
					display("Trying to Accept Alert");
					Thread.sleep(300);
				}
			}
		}
	}

	public static void accept_Optional_Alert(WebDriver webDriver, int counter) throws Exception {
		for (int i = 0; i < counter; i++) {
			try {
				webDriver.switchTo().alert().accept();
				return;
			} catch (Exception e) {
				display("Trying to Accept Alert");
				Thread.sleep(300);
			}
		}
	}

	public static void click_Element(WebDriver webdriver, String locator_Yaml) throws Exception {
		Map locator = getLocator(locator_Yaml);
		find_Element_Loc(webdriver, locator.get("Locator").toString(), locator.get("Value").toString()).click();
	}

	public static void setText_Element(WebDriver webdriver, String locator_Yaml, String textVal) throws Exception {
		Map locator = getLocator(locator_Yaml);
		WebElement click_element = find_Element_Loc(webdriver, locator.get("Locator").toString(),
				locator.get("Value").toString());
		click_element.click();
		click_element.clear();
		click_element.sendKeys(textVal);
	}

	public static void verify_Element_Attribute(WebDriver webdriver, String locator_Yaml, String property_Yaml)
			throws Exception {
		Map locator = getLocator(locator_Yaml);

		WebElement click_element = find_Element_Loc(webdriver, locator.get("Locator").toString(),
				locator.get("Value").toString());

		Map prop = getLocator(property_Yaml);
		String prop_Name = prop.get("PropName").toString();
		String prop_Value = prop.get("PropValue").toString();

		Assert.assertEquals(click_element.getAttribute(prop_Name), prop_Value);

	}

	public static void sendKeys_Element(WebDriver webdriver, String locator_Yaml, String textVal) throws Exception {
		Map locator = getLocator(locator_Yaml);
		WebElement click_element = find_Element_Loc(webdriver, locator.get("Locator").toString(),
				locator.get("Value").toString());
		click_element.sendKeys(textVal);
	}

	public static void focus_window() throws AWTException, InterruptedException {
		final Robot robot = new Robot();
		robot.mouseMove(300, 300);
		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
		Thread.sleep(700);
		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
		Thread.sleep(700);
		robot.keyPress(KeyEvent.VK_ESCAPE);
		robot.keyRelease(KeyEvent.VK_ESCAPE);
		Thread.sleep(700);
		robot.keyPress(KeyEvent.VK_ESCAPE);
		robot.keyRelease(KeyEvent.VK_ESCAPE);
		Thread.sleep(700);
	}

	public static void clear_Env_Chrome() throws InterruptedException, IOException {
		if (System.getProperty("os.name").startsWith("Windows")) {
			Runtime rt = Runtime.getRuntime();
			rt.exec("Taskkill /IM chrome.exe /F");
			rt.exec("Taskkill /IM firefox.exe /F");
			Thread.sleep(1000); // Deepa Sleep needed here.
		}
//		 if (isUnix(systemType())) {
//		 Runtime rt = Runtime.getRuntime();
//		 rt.exec("ps aux | grep chrome | awk ' { print $2 } ' | xargs kill
//		 -9");
//		 rt.exec("ps aux | grep firefox | awk ' { print $2 } ' | xargs kill
//		 -9");
//		 Thread.sleep(1000); // Deepa Sleep needed here
//		 }
	}

	public static void deleteApplication(WebDriver webDriver, String type_Of_App, String status_Of_App)
			throws Exception {

		switch (type_Of_App.toLowerCase() + status_Of_App.toLowerCase()) {
		case "edwosbdraft":
			List<WebElement> deleteElem = webDriver.findElements(By.xpath(
					"//*[@id='certifications']/tbody/tr[ (td[position()=1]/a[contains(text(),'EDWOSB')]) and ( td[ position()=5 and contains(text(),'Draft') ] )  ]/td[ position()=7 ]/a[ contains(text(),'Delete') ]  "));
			if (deleteElem.size() > 0) {
				deleteElem.get(0).click();
				accept_Optional_Alert(webDriver, 8);
			}
			break;
		case "wosbdraft":
			List<WebElement> deleteElem_01 = webDriver.findElements(By.xpath(
					"//*[@id='certifications']/tbody/tr[ (td[position()=1]/a[contains(text(),'WOSB') and not(contains(text(),'EDWOSB'))]) and ( td[ position()=5 and contains(text(),'Draft') ] )  ]/td[ position()=7 ]/a[ contains(text(),'Delete') ]  "));
			if (deleteElem_01.size() > 0) {
				deleteElem_01.get(0).click();
				webDriver.switchTo().alert().accept();
			}
			break;
		case "mppdraft":
			List<WebElement> deleteElem_02 = webDriver.findElements(By.xpath("//*[@id='certifications']/tbody/tr"
					+ "[  " + " ( td[position()=1]/a[contains(text(),'MPP')]       )  and"
					+ " ( td[ position()=5 and contains(text(),'Draft')  ] )  and "
					+ " ( td[ position()=7 ]/a[ contains(text(),'Delete') ] )  " + "]" + "/td[position()=7]/a"));
			if (deleteElem_02.size() > 0) {
				deleteElem_02.get(0).click();
				webDriver.switchTo().alert().accept();
			}
			break;
		}

	}

	public static void clickOnApplicationAllCasesPage(WebDriver webDriver, String type_Of_App) throws Exception {
		// It should be in Vendor Dashboard
		switch (type_Of_App.toLowerCase()) {
		case "wosb":
			webDriver.findElement(By.xpath("//*[@id='certifications']/tbody/tr" + "["
					+ "td[position()=1]/a[contains(text(),'WOSB')]" + "]" + "/td[position()=1]/a")).click();
		case "edwosb":
			webDriver.findElement(By.xpath("//*[@id='certifications']/tbody/tr" + "["
					+ "td[position()=1]/a[contains(text(),'EDWOSB')]" + "]" + "/td[position()=1]/a")).click();
		case "mpp":
			webDriver.findElement(By.xpath("//*[@id='certifications']/tbody/tr" + "["
					+ "td[position()=1]/a[contains(text(),'MPP')]" + "]" + "/td[position()=1]/a")).click();
		}
	}

	public static String returnOrganization_Id(String duns_Number) throws Exception {
		String organization_Id;
		try {
			// See below Start: Need Sleep
			Thread.sleep(3000); // DEEPA: Sleep is needed here since we are
								// querying SQL, and its too fast
			// See below Start

			organization_Id = DatabaseUtils.queryForData(
					"select id from sbaone.organizations where duns_number = '" + duns_Number + "'", 1, 1)[0][0];
		} catch (Exception e) {
			display(e.toString() + ": The Duns number retreival has failed");
			throw e;
		}
		return organization_Id;
	};


	public static void createApplication(WebDriver webDriver, String type_Of_App) throws Exception {
		navigationMenuClick(webDriver, "Programs");
		switch (type_Of_App.toUpperCase()) {
		case "EDWOSB":
			click_Element(webDriver, "JoinNewPgm_Create_App_EDWOSB");
			break;
		case "WOSB":
			click_Element(webDriver, "JoinNewPgm_Create_App_WOSB");
			break;
		case "MPP":
			click_Element(webDriver, "JoinNewPgm_Create_App_MPP");
			break;
		case "8A":
			click_Element(webDriver, "JoinNewPgm_Create_App_8A");
			break;
		default:
			Assert.assertEquals("Edwosb or WOSB or MPP or 8a", "Not Found");
		}
		click_Element(webDriver, "JoinNewPgm_Add_Cert");
		click_Element(webDriver, "Application_Common_Accept_Button");

	}

	public static void searchDuns_Number(WebDriver webDriver, String search_Text) throws Exception {
		click_Element(webDriver, "Search_Duns_Search_Text");
		setText_Element(webDriver, "Search_Duns_Search_Query", search_Text);
		click_Element(webDriver, "Search_Duns_Search_Submit");
	}

	public static void search_Cases_Duns_Number_Table(WebDriver webDriver, String search_Text) throws Exception {
		CommonApplicationMethods.setText_Element(webDriver, "Search_Duns_Cases_Test", search_Text);
		CommonApplicationMethods.click_Element(webDriver, "Search_Duns_Cases_Submit");
	}

	public static void navigationMenuClick(WebDriver webDriver, String which_Button) throws Exception {
		String part_01 = "//nav[@role='navigation']/div/ul/li/a/span[contains(text(),'";
		String part_03 = "')]";
		switch (which_Button.toUpperCase()) {
		case "LOGOUT":
			click_Element(webDriver, "Navigation_Logout");
			break;
		case "HELP":
			click_Element(webDriver, "Navigation_Help");
			break;
		case "CASES":
			click_Element(webDriver, "Navigation_Cases");
			break;
		case "PROGRAMS":
			click_Element(webDriver, "Navigation_Programs");
			break;
		case "DASHBOARD":
			click_Element(webDriver, "Navigation_Dashboard");
			break;
		case "BUSINESS":
			click_Element(webDriver, "Navigation_Business");
			break;
		case "DOCUMENTS":
			click_Element(webDriver, "Navigation_Documents");
			break;
		case "HOME":
			click_Element(webDriver, "Navigation_Home");
			break;
		default:
			Assert.assertEquals("Navigation Menu Not correct", "among present Options");
		}
	}

	public static String getflagvalue() throws Exception {
		String flagforRunfile = FixtureUtils.fixturesDir() + "flagforRunEmailNotification.config";

		BufferedReader bufferedReader = new BufferedReader(new FileReader(flagforRunfile));

		String detailFlag = bufferedReader.readLine();

		return detailFlag;
	}

	public static void casesPageSearch(WebDriver webDriver, String searchValue) throws Exception {
		Map locator = getLocator("Apllication_Case_Search_Text");
		CommonApplicationMethods.setText_Element(webDriver, "Apllication_Case_Search_Text", searchValue);
		locator = getLocator("Apllication_Case_Search_Button");
		CommonApplicationMethods.click_Element(webDriver, "Apllication_Case_Search_Button");
	}
}