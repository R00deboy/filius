; Installer fuer die Filius-Erweiterung (Smartphone, Drucker, Access Point) inkl. Java-Laufzeit.
; Abgeleitet von filius-with-jre.nsi. Installiert sich unter eigenem Namen neben dem offiziellen Filius:
; eigener Ordner, eigener Startmenue-Ordner, eigener Deinstallationseintrag und KEINE .fls-Zuordnung,
; damit das offizielle Filius unberuehrt bleibt.
; Bauen: mvn package, danach  nsis\makensis target\filius-erweiterung.nsi

!define PRODUCT_NAME "Filius Erweiterung"
!define PRODUCT_VERSION "${version}"
!define PRODUCT_PUBLISHER "R00deboy (basierend auf Filius von ${publisher})"
!define PRODUCT_WEB_SITE "https://github.com/R00deboy/filius"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\Filius-Erweiterung.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"
!define STARTMENU_DIR "$SMPROGRAMS\Filius Erweiterung"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; License page
!insertmacro MUI_PAGE_LICENSE "GPLv3.txt"
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "French"

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "Filius-Erweiterung-Setup-${PRODUCT_VERSION}.exe"
InstallDir "$PROGRAMFILES64\Filius Erweiterung"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
RequestExecutionLevel admin
ShowInstDetails show
ShowUnInstDetails show

Section "Filius Erweiterung" SEC01
  AddSize 60000
  SetShellVarContext all

  SetOutPath "$INSTDIR"
  SetOverwrite try
  File "Changelog.md"
  File "Readme.md"
  File "Einfuehrung_Filius.pdf"
  File "Introduction_Filius.pdf"
  File "Filius.exe"
  File "filius.jar"
  File "Filius.sh"
  File "Filius.command"
  File "GPLv2.txt"
  File "GPLv3.txt"
  File "..\FILIUS-ERWEITERUNG.md"
  File /r "classes\config"
  File /r "classes\hilfe"
  File /r "classes\img"
  File /r "lib"
  File /r "classes\tmpl"
  File /r "..\java-runtime"

  CreateDirectory "${STARTMENU_DIR}"
  CreateShortCut "${STARTMENU_DIR}\Filius Erweiterung.lnk" "$INSTDIR\Filius.exe"
  CreateShortCut "${STARTMENU_DIR}\Filius Einfuehrung.lnk" "$INSTDIR\Einfuehrung_Filius.pdf"
  CreateShortCut "${STARTMENU_DIR}\Filius Introduction.lnk" "$INSTDIR\Introduction_Filius.pdf"
  CreateShortCut "${STARTMENU_DIR}\Neue Geraete (Beschreibung).lnk" "$INSTDIR\FILIUS-ERWEITERUNG.md"
SectionEnd

Section -AdditionalIcons
  SetShellVarContext all
  SetOutPath $INSTDIR
  WriteIniStr "$INSTDIR\${PRODUCT_NAME}.url" "InternetShortcut" "URL" "${PRODUCT_WEB_SITE}"
  CreateShortCut "${STARTMENU_DIR}\Projekt auf GitHub.lnk" "$INSTDIR\${PRODUCT_NAME}.url" "" "$INSTDIR\Filius.exe"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "${PRODUCT_DIR_REGKEY}" "" "$INSTDIR\Filius.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\Filius.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd


Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) wurde erfolgreich entfernt." /SD IDOK
FunctionEnd

Function un.onInit
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "$(^Name) und alle Komponenten entfernen?" /SD IDYES IDYES +2
  Abort
FunctionEnd

Section Uninstall
  SetShellVarContext all

  Delete "${STARTMENU_DIR}\Filius Erweiterung.lnk"
  Delete "${STARTMENU_DIR}\Filius Einfuehrung.lnk"
  Delete "${STARTMENU_DIR}\Filius Introduction.lnk"
  Delete "${STARTMENU_DIR}\Neue Geraete (Beschreibung).lnk"
  Delete "${STARTMENU_DIR}\Projekt auf GitHub.lnk"
  RMDir "${STARTMENU_DIR}"

  RMDir /r "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  ; Die .fls-Zuordnung gehoert dem offiziellen Filius und wird hier bewusst nicht angefasst.
  SetAutoClose true
SectionEnd
