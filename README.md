![HEYA Logo](https://raw.githubusercontent.com/frpnit/HeyaLauncher/master/screenshots/HEYA_logo.png "Heya Logo") HEYA Launcher
=========

HEYA Launcher is an alternative launcher for Ouya, which the main unique feature is Child Mode: it brings to you the possibility of parental controls, restricting access to inapropriate games/apps or system areas that could mess everything.

Interested in giving it a try? You can download at releases page: <a href="https://github.com/frpnit/HeyaLauncher/releases">click here</a>.

It consists of two APKs, install "Heya Launcher" first to ensure everything will work alright.

<br/>
### Features
- A basic, clean and sleek interface, with visuals that had some resemblance with standard Ouya launcher.
- Everything should work with stock Ouya - no need for ROOT.
- Language options: English and Brazilian Portuguese.
- Child Mode that restricts: games/apps(by hiding them); Ouya launcher home access(will always send you back to Heya); lauching and downloading games/apps from "You May Also Like" section on System Overlay (both aren't allowed); access to System and Ouya areas like Discover, Manage, etc.;
- Lets you customize: launcher's name, icon size, icon appearance order, language, BG image customization.
- Searches for paired and turned on/plugged controller at boot. If nothing detected, launches Controller Pairing after one minute. HID devices like mouse and keyboard will not be taken in account.
- Can run one game/app automatically at launcher's startup.
- Direct access to some important Ouya Function Screens, like Discover or Manage.
- Can show/hide Android System Apps if needed (note: Google Play is considered by launcher as a normal app and not a system one).
- Export and Import settings in case of backup / resetting needs.

Be sure to read about Child Mode on its settings dialog in HEYA. It explains everything that happens when entering Child Mode, how to exit and everything you need to know.

<br/>
### Child Mode
Since it's the most different feature of HEYA, i think it would be better giving details about it. Child Mode brings:
- Games/Apps hiding: You can select them on a list and they won't appear on games/apps list screen! And more: if accessed from any other game/app, it will be denied - it will stay on game/app you were using before.

- System Apps hiding: System Apps won't be accessible.

- "Ouya Home" overriding: It's annoying to block access to games and all but still that "Ouya Home" option on "System Menu" Overlay that sends you back to default launcher - all the effort is gone! But no more, with Heya there is a routine that everytime you press "Ouya Home", it will revert back to Heya and not default launcher anymore.

- "You May Also Like" blocking: on "System Menu" overlay, there are three games/apps recomendations wich you can download or launch. Obviously, that's not good! On Heya, both actions are denied - downloads keeps being deleted and games/apps launching are denied the same way as if they were an hidden games/apps. There is even a strip overlay that covers those 3 recomendations to difficult/avoid user to move there and click on things.

- Some launcher access restraining: It hides some menus that would break Child Mode purpose, like Discover access, Hidden Games/Apps access, and so on.

- App overlay disabling: It avoids children access to game details and uninstall screens.

- Startup Game/App blocking: it won't be executed because could be a forbidden one.

<br/>
### Screenshots
####Home Screen
![Heya Screenshot](https://raw.githubusercontent.com/frpnit/HeyaLauncher/master/screenshots/01_HEYA_home.png "Heya Screenshot")

####App Overlay
![Heya Screenshot](https://raw.githubusercontent.com/frpnit/HeyaLauncher/master/screenshots/02_HEYA_app_overlay.png "Heya Screenshot")

####System Menu Overlay
![Heya Screenshot](https://raw.githubusercontent.com/frpnit/HeyaLauncher/master/screenshots/03_HEYA_system_menu_overlay.png "Heya Screenshot")

####System Screen
![Heya Screenshot](https://raw.githubusercontent.com/frpnit/HeyaLauncher/master/screenshots/04_HEYA_system.png "Heya Screenshot")

####Settings Screen
![Heya Screenshot](https://raw.githubusercontent.com/frpnit/HeyaLauncher/master/screenshots/05_HEYA_settings.png "Heya Screenshot")

####Settings - Hidden Apps
![Heya Screenshot](https://raw.githubusercontent.com/frpnit/HeyaLauncher/master/screenshots/06_HEYA_settings_hidden_apps.png "Heya Screenshot")

<br/>
### Two APKs? Why?
Believe me, i just wanted to avoid it as much as you, but at the end it was inevitable. At least i couldn't find another solution. The service that takes care of Child Mode functions needs to run apart because Ouya system just KILLS 3rd party launchers everytime one execute games/apps from "You May Also Like" section on "System Menu" overlay. If the service was embedded on same launcher's APK, it would be killed aswell so it would have been a Child Mode breach.

<br/>
### Acknowledgements and Licensing
- HEYA is based on Firestarter launcher for FireTV, made by Lukas Berger, and released under the same <a href="https://www.mozilla.org/en-US/MPL/2.0/" target="_blank">MPLv2 license</a>.
You can check Firestarter <a href="https://github.com/sphinx02/FireStarter" target="_blank">https://github.com/sphinx02/FireStarter</a>
- Many thanks to Ouyaforum nice and supportive people - specially Eldon McGuinness for answers, for sending me Controller Pairing source code, and for some more (direct or indirect) guidance.

<br/>
### Final Notes
Making this launcher was a big lesson to me - I've never really programmed in Java, and things got worse because i had to learn some Android specifics. Of course, i've started uppon a work from someone else, but it was still a challenge bigger than i thought it would be. I tried many things. I really mean MANY. But being able to do such an app, was really rewarding. I'm very happy and proud of myself :)

Ah, and for those who ends up trying it: I hope you enjoy my work! And sorry about my english :D

