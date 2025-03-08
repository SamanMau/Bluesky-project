# BlueSky Project

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Tech Stack](#tech-stack)
4. [Project Structure](#project-structure)
5. [Installation & Setup](#installation--setup)
   - [Supported Platforms](#supported-platforms)
   - [Prerequisites](#prerequisites)
   - [Setup Instructions](#setup-instructions)
   - [Usage of the Application](#usage-of-the-application)
6. [Troubleshooting](#troubleshooting)
7. [APIs and External Tools](#apis-and-external-tools)
8. [Contributors](#contributors)
9. [Future Enhancements](#future-enhancements)
10. [License](#license)
11. [Contact](#contact)

---

## Introduction
**The BlueSky Project** is a web service where users can write tweets and have them spell-checked before they are published. The tweets are analyzed using the LIBRIS spelling API, and the user receives spelling suggestions to correct any errors. After approval, tweets can be published and disseminated.

---

## Features
- **Responsive Design:** A user-friendly interface built with modern web technologies.
- **Real-Time Spell Checking:** Integration with LIBRIS API for accurate spelling corrections.
- **Post Editing:** Simple, distraction-free text editing for composing tweets.
- **Publishing to Bluesky:** Direct API integration with Bluesky for publishing.
- **Scalable Architecture:** Modular design supporting future feature expansions.
- **Version Control:** Project managed with Git and GitHub for collaboration and version tracking.

---

## Tech Stack
**Frontend:**
- HTML
- CSS
- JavaScript

**Backend:**
- Java (SpringBoot)
- Maven

**APIs & Tools:**
- LIBRIS API (Spell Checking)
- Bluesky API (Publishing)
- Git & GitHub (Version Control)

---

## Project Structure
```
BLUESKY-PROJECT/
│
├── .idea/                 # Konfigurationsfiler för IntelliJ IDEA-projektet.
│   ├── compiler.xml       # Inställningar för kompilatorn.
│   ├── encodings.xml      # Konfiguration för teckenkodning.
│   ├── jarRepositories.xml# Information om jar-repositorier.
│   ├── misc.xml           # Diverse projektkonfigurationer.
│   ├── modules.xml        # Modulkonfigurationer för projektet.
│   ├── Twitter-project.iml# Modulfil för IntelliJ IDEA-projektet.
│   ├── uiDesigner.xml     # Konfiguration för UI-designverktyg.
│   └── vcs.xml            # Versionskontrollinställningar.
│
├── .vscode/               # Inställningar för Visual Studio Code (om tillämpbart).
│
├── Twitter/               # Projektets huvudmapp.
│   ├── src/               # Källkodsmapp.
│   │   ├── main/          # Primär källkod.
│   │   │   ├── java/      # Java-källkodsmapp.
│   │   │   │   └── com/example/Twitter/
│   │   │   │       ├── Controller/
│   │   │   │       │   ├── ApiAuthentication.java # Hanterar API-autentisering.
│   │   │   │       │   └── BlueSky_Controller.java# Kontroller för Bluesky-relaterade funktioner.
│   │   │   │       ├── Service/
│   │   │   │       │   ├── LibrisManager.java    # Hanterar integrationen med LIBRIS-API.
│   │   │   │       │   └── TwitterApplication.java # Startpunkt för applikationen.
│   │   │   ├── resources/ # Resursfiler för applikationen.
│   │   │   │   ├── static/ # Statiska filer (CSS, JS, HTML).
│   │   │   │   │   ├── css/
│   │   │   │   │   │   ├── common.css           # Gemensam CSS för applikationen.
│   │   │   │   │   │   └── styles.css           # Ytterligare stildefinitioner.
│   │   │   │   │   ├── js/
│   │   │   │   │   │   └── app.js               # JavaScript-logik för frontend.
│   │   │   │   │   └── login.html               # Login sida för frontend.
│   │   │   │   └── application.properties       # Konfiguration för Spring Boot-applikationen.
│   │   └── test/          # Tester för applikationen.
│   │       └── java/com/example/Twitter/
│   │           └── TwitterApplicationTests.java # Enhetstester för applikationen.
│
├── target/                # Kompilerad kod och byggresultat.
│
├── HELP.md                # Hjälpfil för projektet.
├── mvnw                   # Wrapper för Maven-verktyget.
├── mvnw.cmd               # Windows-version av Maven-wrapper.
├── pom.xml                # Maven-konfigurationsfil.
├── .env                   # Miljövariabler för utvecklingsmiljö.
├── .gitignore             # Lista över filer som ska ignoreras av Git.
├── mock.env               # Exempel på miljövariabler.
├── README.md              # Projektbeskrivning och instruktioner.
└── userText.json          # JSON-fil som hanterar användardata.
```
---

## Installation & Setup

### Supported Platforms
The BlueSky project is compatible with the following systems:
* **Operating Systems:**
  - Windows 10, 11
  - macOS 10.15 or later
* **Recommended Software:**
  - Visual Studio Code

---

### Prerequisites
To run the Bluesky project, ensure the following are installed on your system:
- **Java 17 or higher (JDK)**
- **Maven 3.9.1 or higher**
- **Git Bash or equivalent terminal**
- **GitHub account**

---

### Setup Instructions
#### Windows Setup

1. **Install Java (JDK):**
    - Navigate to the Oracle [JDK Downloads](https://www.oracle.com/java/technologies/downloads/).
    - Download the x64 Installer and follow the instructions.
    - After installation, navigate to the JDK bin folder (e.g., ```C:\Program Files\Java\jdk-23\bin```). Copy the path for later use.
    - Set up the environment variable:
        - Search for Edit environment variables for your account in the Start menu.
        - Select Path and click Edit....
        - Add the copied path, press **Enter**, and click **OK**.

2. Install Visual Studio Code:
    - Go to the [VS Code Downloads](https://code.visualstudio.com/download).
    - Download the Windows installer and follow the instructions.
    - Once inside VS Code, press ```Ctrl + Shift + X``` to open the extensions marketplace.
    - Install Extensions: *Extension Pack for Java*, *Live Server Preview*.

3. Install Maven:
    - Download Maven from the [Apache Maven Downloads](https://maven.apache.org/download.cgi).
    - Extract the binary ZIP archive and note the path to the **bin** folder (e.g., ```C:\Users\YourName\Downloads\apache-maven-3.9.1\bin```).
    - Set the Maven path in your environment variables (**similar to the JDK setup, see above**).
    - Use Command Prompt or Git Bash to navigate to the folder containing **pom.xml** (e.g., ```cd C:\Users\YourName\Downloads\Bluesky-project```).
    - Run **mvn clean install** to download all dependencies.

4. Clone the Repository:
    - Visit the [GitHub Repository](https://github.com/SamanMau/Bluesky-project).
    - Click the **Code** button and select **Download ZIP**.
    - Extract the ZIP file to a folder on your computer.
    - Open that folder on VS Code.

5. **Install Dependencies:**
   - Open a terminal, navigate to the project folder, and run:
     ```bash mvn clean install```

---

#### MacOS Setup
1. Install Java (JDK):
    - Navigate to the Oracle [JDK Downloads](https://www.oracle.com/java/technologies/downloads/).
    - Download the Arm64 DMG Installer and follow the instructions.

2. Install Visual Studio Code:
    - Go to the [VS Code Downloads](https://code.visualstudio.com/download).
    - Download the Mac installer and follow the instructions.
    - Once inside VS Code, press ```Ctrl + Shift + X``` to open the extensions marketplace.
    - Install: *Extension Pack for Java*, *Live Server Preview*.

3. **Install Maven:**
   - Install Homebrew:
     ```bash /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"```
   - Install Maven:
     ```bash brew install maven```
   - Verify Maven & Brew installation:
     ```bash mvn -v```
     ```brew --version```

4. **Clone the Repository & Install Dependencies:**
   - Same steps as Windows setup.

---

#### File Navigations (if you were not to find the file pathways):
- Java Main Class (TwitetrApplication.java): src/main/java/TwitterApplication.java
- HTML file: src/main/resources/static/login.html
- Configuration file: ./pom.xml

---

### Usage of the Application
1. Open **VS Code** and navigate to the project folder in it.
2. **Run the HTML application:**
    - Open `login.html` in VS Code.
    - Path to file: ```src/main/resources/static/login.html```
    - Click the "**Go Live**"- button in the **bottom-right corner of VS Code** (requires the *Live Server Extension*!).
3. **Start the Mashup Service**
    - Run the backend `TwitetrApplication.java` class (press the button "**Run**" below the classname).
    - Path to class: ```src/main/java/TwitterApplication.java```
5. **Using the Application:**
    - Write your text in the input box labeled "Write your post here....".
    - Click on the button "**Check Spelling**" to analyze and correct errors by getting spelling suggestions under the "Suggestions:"-field.
    - Accept the suggestion (if there's any spelling errors).
    - Click "**Publish your text**"-button to post on Bluesky.

---

## Troubleshooting
1. **Java Not Recognized:**
- Ensure the JDK path is correctly set in environment variables.

2. **Maven Command Fails:**
- Check if Maven is installed and the path is configured.

3. **VS Code Extensions Missing:**
- Verify that Extension Pack for Java and Live Server Preview are installed.

If issues persist, consult the [project repository](https://github.com/SamanMau/Bluesky-project) or contact the project maintainers.

---

## APIs and External Tools
1. **LIBIRIS API:** Utilized for advanced spell-checking and linguistic analysis. Configuration settings are stored in **Twitetr/src/main/resources/application.properties**.
2. **BlueSky API:**  Enhances the application's external integration features. Ensure you add your API key in the env file (at the /root).
3. **Git & GitHub:** All code changes, pull requests, and issue tracking are handled using Git version control and the GitHub platform.

---

## Contributors
- Mustafa - [GitHub Profile](https://github.com/mustafa-mahamud-mohammed-2004) (Front-end Developer, UI/UX Designer)
- Amjad - [GitHub Profile](https://github.com/Sharqawi02) (Front-end Developer, UI/UX Designer)
- Adde - [GitHub Profile](https://github.com/kingabdull02) (Back-end Developer, UI/UX Designer)
- Saman - [GitHub Profile](https://github.com/SamanMau) (Back-end Developer, UI/UX Designer)
- Karam - [GitHub Profile](https://github.com/karamkallab) (Back-end Developer, UI/UX Designer)

---

## Future Enhancements
- **User Authentication:** Implement OAuth 2.0 to provide secure and seamless login functionality.
- **Advanced Analytics:** Introduce data visualization tools to track and analyze user interactions.
- **Mobile Responsiveness:** Optimize the user interface for improved mobile device compatibility.
- **Enhanced Text Analysis:** Incorporate advanced grammar-checking algorithms for better accuracy.
- **Social Platform Integration:** Enable seamless sharing and interaction with other social media platforms.
- **Customizable UI:** Offer users additional options to personalize their experience.
- **Dark Mode Support:** Introduce a dark mode feature for better accessibility and user comfort.
- **Multi-language Support:** Enable translations for a more inclusive user experience.
- **Offline Mode:** Allow users to access core functionalities without an internet connection.

---

## License
This project **does not have a license**. All rights are reserved. Contact the [project owner](saman_jejo@hotmail.com) for permissions.

---

## Contact
For any questions, queries or support, feel free to reach out to us:

- **Mustafa Mahamud Mohammed:** mustafa.mohammed.business@gmail.com
    - **GitHub:** https://github.com/mustafa-mahamud-mohammed-2004
- **Abdulkadir Adde:** addeabdulkadir@gmail.com
    - **GitHub:** https://github.com/kingabdull02
- **Amjad Sharqawi:** amjad.sharqawi02@gmail.com
    - **GitHub:** https://github.com/Sharqawi02
- **Saman Jejo:** saman_jejo@hotmail.com
    - **GitHub:** https://github.com/SamanMau
- **Karam Kallab:** karamkallab693@gmail.com
    - **GitHub:** https://github.com/karamkallab

<br>

Thank you for using Bluesky! We hope you enjoy the application.
