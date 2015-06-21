package com.github.zxkane.wechat.moments;

import static java.util.Arrays.asList;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	public static class SelendroidDriver extends AndroidDriver implements
			HasTouchScreen {

		public RemoteTouchScreen touch;

		public SelendroidDriver(URL remoteAddress,
				Capabilities desiredCapabilities) {
			super(remoteAddress, desiredCapabilities);
			touch = new RemoteTouchScreen(getExecuteMethod());
		}

		@Override
		public TouchScreen getTouch() {
			return touch;
		}
	}

	public static final Logger logger = LoggerFactory.getLogger("MomentsApp");

	public static final int WAIT_INTERVAL_XXS = 100;
	public static final int WAIT_INTERVAL_XS = 500;
	public static final int WAIT_INTERVAL_S = 1000;
	public static final int WAIT_INTERVAL_M = 3000;
	public static final int WAIT_INTERVAL_L = 5000;
	public static final int WAIT_INTERVAL_XL = 10000;

	public static final String SEARCH_TEXT_VIEW = "//android.widget.TextView[@content-desc='Search' or @content-desc='搜索']";
	public static final String CONTACT_SEARCH_EDITTEXT = "//android.widget.EditText";
	public static final String CONTACT_FOUND_TEXT_VIEW = "//android.widget.TextView[@text='WeChat ID:%s' or @text='微信号: %s']";
	public static final String CHAT_INFO_TEXT_VIEW = "//android.widget.TextView[@content-desc='Chat Info' or @content-desc='聊天信息']";
	public static final String NICKNAME_TEXT_VIEW = "//android.widget.TextView[@text='%s']";
	public static final String ALBUM_TEXT_VIEW = "//android.widget.TextView[@text='Album' or @text='个人相册']";
	public static final String MOMENT_INDEX_VIEW = "//android.widget.ListView[1]/android.widget.LinearLayout[%s]";
	public static final String GENERIC_VIEW = "android.view.View";
	public static final String TEXT_VIEW = "android.widget.TextView";
	public static final String LINEAR_LAYOUT = "android.widget.LinearLayout";
	public static final String MOMENT_DATE_VIEW = MOMENT_INDEX_VIEW
			+ "/android.widget.LinearLayout[2]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.LinearLayout[1]/android.widget.TextView";
	public static final String MOMENT_TEXT_VIEW = MOMENT_INDEX_VIEW
			+ "/android.widget.LinearLayout[2]/android.widget.LinearLayout[2]/android.widget.LinearLayout[1]/android.widget.LinearLayout/android.widget.TextView";
	public static final String MOMENT_PHOTO_VIEW = MOMENT_INDEX_VIEW
			+ "/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.view.View";
	public static final String PHOTO_IMAGE_VIEW = "//android.widget.Gallery/android.widget.ImageView";
	public static final String DOWNLOAD_IMAGE = "//android.widget.TextView[@text='保存到手机']";
	public static final String MORE_TEXT_VIEW = "//android.widget.TextView[@content-desc='更多']";

	public static void main(String[] args) throws IOException,
			InterruptedException {
		OptionParser parser = new OptionParser() {
			{
				accepts("s").withOptionalArg().ofType(URI.class)
						.describedAs("serverURL")
						.defaultsTo(URI.create("http://127.0.0.1:4723/wd/hub"));
				accepts("c").withRequiredArg().ofType(String.class)
						.describedAs("contactId");
				acceptsAll(asList("v", "verbose", "talkative", "chatty"),
						"be more verbose");
				acceptsAll(asList("h", "?"), "show help").forHelp();
			}
		};

		OptionSet options = parser.parse(args);

		if (!options.has("c"))
			parser.printHelpOn(System.out);

		new App().run(options.valueOf("c").toString(), options.valueOf("s")
				.toString());
	}

	private AppiumDriver driver = null;

	private void run(final String wechatId, final String serverURL)
			throws IOException, InterruptedException {

		cleanWeiXinFiles();

		int exitCode = 0;

		URL serverUrl = new URL(serverURL);
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("automationName", "Appium");
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("platformVersion", "4.2.2");
		capabilities.setCapability("deviceName", "Android");
		capabilities.setCapability("appPackage", "com.tencent.mm");
		capabilities.setCapability("appActivity", ".ui.LauncherUI");
		capabilities.setCapability("noReset", "true");
		capabilities.setCapability("newCommandTimeout", "86400");
		capabilities.setCapability("unicodeKeyboard", "true");
		capabilities.setCapability("resetKeyboard", "true");

		try {
			logger.trace("Creating driver ... ", false);
			driver = new SelendroidDriver(serverUrl, capabilities);

			WebDriverWait driverXLWait = new WebDriverWait(driver,
					WAIT_INTERVAL_XL / 1000);
			WebDriverWait driverMWait = new WebDriverWait(driver,
					WAIT_INTERVAL_M / 1000);
			WebDriverWait driverSWait = new WebDriverWait(driver,
					WAIT_INTERVAL_S / 1000);

			logger.trace("Wait at most {} secs for init.",
					WAIT_INTERVAL_XL / 1000);
			driverXLWait.until(ExpectedConditions.visibilityOfElementLocated(By
					.xpath(SEARCH_TEXT_VIEW)));

			logger.trace("Finding and clicking search button");
			WebElement search = driver.findElement(By.xpath(SEARCH_TEXT_VIEW));
			search.click();

			logger.trace("Locate contact search text");
			driverSWait.until(ExpectedConditions.visibilityOfElementLocated(By
					.xpath(CONTACT_SEARCH_EDITTEXT)));
			WebElement elmInputWeixinID = driver.findElement(By
					.xpath(CONTACT_SEARCH_EDITTEXT));
			elmInputWeixinID.clear();
			logger.trace("Type in contact id '{}' ... ", wechatId);
			elmInputWeixinID.sendKeys(wechatId);

			logger.trace("Finding expected contact result");
			String nickName = null;
			try {
				driverMWait.until(ExpectedConditions
						.visibilityOfAllElementsLocatedBy(By.xpath(String
								.format(CONTACT_FOUND_TEXT_VIEW, wechatId,
										wechatId))));
				WebElement contact = driver.findElement(By.xpath(String.format(
						CONTACT_FOUND_TEXT_VIEW, wechatId, wechatId)));
				WebElement parentElement = driver.findElementByXPath(String
						.format(CONTACT_FOUND_TEXT_VIEW, wechatId, wechatId)
						+ "/..");
				nickName = parentElement
						.findElement(By.className(LINEAR_LAYOUT))
						.findElement(By.className(TEXT_VIEW)).getText();
				contact.click();
			} catch (NoSuchElementException e) {
				logger.error("The specified Wechat ID '{}' can't be found.",
						wechatId);
				throw new ExitException(-1);
			}

			logger.trace("Going to the profile of contact");
			driverMWait.until(ExpectedConditions.presenceOfElementLocated(By
					.xpath(CHAT_INFO_TEXT_VIEW)));
			driver.findElement(By.xpath(CHAT_INFO_TEXT_VIEW)).click();
			driverMWait.until(ExpectedConditions.visibilityOfElementLocated(By
					.xpath(String.format(NICKNAME_TEXT_VIEW, nickName))));
			driver.findElement(
					By.xpath(String.format(NICKNAME_TEXT_VIEW, nickName)))
					.click();

			logger.trace("Going to the album of contanct");
			try {
				driverMWait.until(ExpectedConditions
						.visibilityOfElementLocated(By.xpath(ALBUM_TEXT_VIEW)));
				driver.findElement(By.xpath(ALBUM_TEXT_VIEW)).click();
			} catch (TimeoutException e) {
				logger.info("Wechat ID '{}' does not have public album.",
						wechatId);
				throw new ExitException(0);
			}

			scollAndProcess(driver, driverMWait, 1);
		} catch (NoSuchElementException e) {
			logger.error("Failed to find expected element.", e);
		} catch (ExitException e) {
			exitCode = e.exitCode;
		} catch (Exception e) {
			logger.error("Exception:" + e.getMessage(), e);
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
		System.exit(exitCode);
	}

	private void scollAndProcess(AppiumDriver driver, WebDriverWait driverWait,
			int times) throws InterruptedException, IOException {
		logger.trace("Scoll down album for viewing more");
		TouchActions touch = new TouchActions(driver).flick(
				driver.findElement(By.xpath("//android.widget.ListView")), 0,
				-300, 50);
		touch.perform();
		Thread.sleep(WAIT_INTERVAL_M);

		logger.trace("Browsing moments...");
		int index = 1;
		try {
			while (true) {
				WebElement moment = driver.findElement(By.xpath(String.format(
						MOMENT_INDEX_VIEW, index)));
				logger.trace("Processing the moment #{}...", index);
				processMoment(moment, driver, index, driverWait);
				index++;
			}
		} catch (NoSuchElementException e) {
			// FIXME better way to find out already reaching the bottom
			if (times < 2)
				scollAndProcess(driver, driverWait, times + 1);
		}
	}

	private void processMoment(WebElement moment, AppiumDriver driver,
			int index, WebDriverWait driverMWait) throws IOException,
			InterruptedException {
		List<WebElement> dateViews = driver.findElements(By.xpath(String
				.format(MOMENT_DATE_VIEW, index)));
		// TODO reuse previous date information if list is empty
		dateViews.forEach(v -> {
			logger.trace("\t\tmoment was published at {}", v.getText());
		});

		try {
			WebElement photoView = driver.findElement(By.xpath(String.format(
					MOMENT_PHOTO_VIEW, index)));
			String momentText = driver.findElement(
					By.xpath(String.format(MOMENT_TEXT_VIEW, index))).getText();
			logger.trace("\t\tmoment was published with content '{}'.",
					momentText);

			photoView.click();
			try {
				logger.trace("\tOpening photos of moment...");
				driverMWait.until(ExpectedConditions
						.visibilityOfAllElementsLocatedBy(By
								.xpath(PHOTO_IMAGE_VIEW)));
				// WebElement imageView =
				// driver.findElement(By.xpath(PHOTO_IMAGE_VIEW));
				// TODO swipe to next image and save it
				driver.findElement(By.xpath(MORE_TEXT_VIEW)).click();
				driver.findElement(By.xpath(DOWNLOAD_IMAGE)).click();

				ZipInputStream input = new ZipInputStream(
						new ByteArrayInputStream(driver
								.pullFolder("/sdcard/tencent/MicroMsg/WeiXin")));
				try {
					ZipEntry entry;
					while ((entry = input.getNextEntry()) != null) {
						logger.trace("\t\t\tGot saved image file: {}",
								entry.getName());
						// TODO put your logic here to process the pic
					}
				} finally {
					input.close();
				}
			} catch (IOException e) {
				logger.error(
						"Failed to pull the saved photos back from device.", e);
			} catch (TimeoutException e) {
				try {
					driver.findElement(By
							.xpath("//android.widget.LinearLayout/android.widget.RelativeLayout/android.widget.ImageView"));
					logger.trace("\t\tmoment was shared with video can't be saved.");
				} catch (NoSuchElementException e1) {
					throw e;
				}
			} finally {
				try {
					cleanWeiXinFiles();
				} finally {
					driver.navigate().back();
				}
			}
		} catch (NoSuchElementException e) {
			// TODO only text without photo
			logger.trace("\t\tmoment was published without original photos.");
		}
	}

	CommandPrompt cmd = new CommandPrompt();

	private void cleanWeiXinFiles() throws IOException, InterruptedException {
		cmd.runCommand("adb shell rm -f /sdcard/tencent/MicroMsg/WeiXin/*");
	}
}
