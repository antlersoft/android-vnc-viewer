A VNC viewer for Android platform. android-vnc-viewer is forked from tightVNC viewer.

This project is still under development. Your feedback is highly appreciated.

**android-vnc-viewer** _is now available on Android Market.  The latest development builds will still be available here._

See the [User Documentation Wiki Page](Documentation.md) and the updated [FAQ](faq.md).

**Latest version (Build 203 - 20110327)**
  * Add Chinese translation (thanks to wangp.gm)
  * New icon

**Version (Build 197 - 20110321)**
  * Update intro text slightly
> This version corresponds to 0.5.0 in the Android Market

**Version (Build 195 - 20110320)**
  * Import/export settings
  * Button in zoom control to bring up keyboard
  * Hungarian translation

**Version (Build 187 - 20101119)**
  * More informative messages on connection failure
  * No longer prefers to install to SD, since that was causing problems; will still install to SD on demand
  * Fix for crash when screen dimensions change (some devices)
  * Option to force tiling mode (for very large screens where auto guesses wrong)
  * Sent text is remembered across sessions (optionally)

**Version (Build 182 - 20100819)**
  * Support for UltraVNC windows authentication
  * Meta-characters for Hangul input

**Version (Build 177 - 20100725)**
  * Pinch zoom on 2.0+ devices in gesture modes
> This version correspons with 0.4.7 in Android Market

![http://android-vnc-viewer.googlecode.com/files/screen_shot.png](http://android-vnc-viewer.googlecode.com/files/screen_shot.png)

**Version (Build 175 - 20100724)**
  * Fix sending upper case characters from physical keyboad (some VNC servers)
  * Fix crash bug when phone display bigger than remote display
  * Fix crash bug when you press the menu button too soon :)

**Version (Build 166 - 20100722a)**
  * Quick fix for local mouse cursor
> This version corresponds with 0.4.6 in Android Market

**Version (Build 163 - 20100722)**
  * Bug fixes -- fix Froyo-only bug with shortcuts
  * UI tweaks

**Version (Build 158 - 20100720)**
  * Less memory use; supports larger monitors without "green flash"
  * French Translation (thanks to Josue Saury)
  * Intro dialog

**Version (Build 141 - 20100706)**
  * Multi-touch pan in touchpad mode
  * Save on SD with Froyo
  * Fix bug with shortcuts
> This version corresponds to 0.4.3 on Android Market.

**Version (Build 20100701)**
  * New touchpad mode, where screen works like mousepad; thanks to Xingang Huang.
  * Improved mouse control from DPad.

**Version (Build 20100328)**
  * Fixes for repeater functionality
  * Ability to create shortcuts to VNC Connections

**Version (Build 20100317)**
  * Support for UltraVNC-type repeaters
  * Minor memory use enhancements

**Version (Build 20100126)**
  * D-Pad control merged into default (touch pan/zoom/mouse) control mode; D-pad specific mode removed.  This should be simpler for all concerned.

**Version (Build 20100125)**
  * New gesture input mode for phones without trackballs; use D-pad for fine cursor control
  * Improved Italian translation (thanks to Diego Pierotto)
  * Japanese translation (thanks to Mike Markey)

**Version (Build 20100102)**
  * Move mouse scroll-wheel continuously by holding down volume buttons
  * (Partial) Italian translation for Italian locale

**Version (Build 20091217)**
  * Small tweaks to the previous; mainly enabling input modes for both Zoomable and One-to-One scaling.

**Version (Build 20091216)**
  * This version adds (finally) a new Scaling mode -- Zoomable.  Along with the scaling mode comes a new input mode where you can both pan and control the mouse with the touchscreen and gestures.  You pan by dragging or flinging on the display; you click the mouse by tapping the display.  You right-click by double-tapping the display (or by holding down the camera button while tapping).  You drag by doing a long press on the display, and then dragging.
> Starting with this version, the development versions will use the same certificate as the market version.  This means you should be able to switch between market and development versions without doing an uninstall; it also means that **you must [uninstall](Uninstall.md) previous development versions before this version will install.**  That means you'll lose your configurations; however, with market certificate compatibility, you shouldn't need to lose configurations again after this when switching versions.

**Version (Build 20091118)**
  * Add new control option to pan with the directional pad and touch to control the mouse pointer.  This is intended to support devices without a trackball.  I'd appreciate user reports on its usefulness.  Note that this will not work well on phones with a trackball.

**Version (Build 20091107)**
  * Fixes to directly support devices with different size screens

**Version (Build 20091030)**

  * Experimental performance improvements on partial screen updates.  The screen will flash green at times; this is normal but if portions of the screen **remain stuck** green (or black) I would appreciate a bug report describing circumstances (host screen resolution, user actions...)
  * Option to have the mouse follow pans around the screen

**Version (Build 20091006)**

  * Enter text goes to first menu page.  Keeps history of text entered and bug fix to allow uppercase text.

**Version (Build 20090918)**

  * Add menu command (Enter text...) to allow you to enter a block of text to convert to keystrokes to send to your VNC server.  This is to support the Magic and other phones that lack a physical keyboard.


**Version (Build 090307)**

There are many fixes and added features in this release.

  * Add support for the meta key editor to send special keys and keys with Shift/Ctrl/Alt modifiers
  * Add support for a local mouse cursor
  * Make the phone's volume buttons work as the mouse scroll wheel
  * Blank invalid sections of the screen when scrolling tiles
  * Add help menu item
  * Add custom icon
  * Fix menu shortcut keys being sent the server

**(Build 090131)**

Most usable version yet...
  * Fix bug where trackball mouse would always drag
  * Save input mode and scale settings with connection configuration
  * New menu option; Pan Follows Mouse-- will pan screen to follow mouse as you move it around VNC display


**(Build 090125):**
  * Works with large VNC displays with tiling
  * Saves configurations
  * Pick color mode before connecting to work with OS/X vnc server
  * Supports trackball mouse (and other input modes)
  * Sends all symbols on G1 keyboard
  * Right mouse-button support (with Camera button)
  * Esc key support
  * Send Ctrl-Alt-Del

This project was initially launched on SDK m5, and most recently, has been modified to work on the G1 and 1.0 SDK as a viewer. With James Moger's help, now it is a fairly complete VNC viewer. **Thanks James!**

**Major Updates by James (Build 081205):**
  * Ported all other encodings except Tight.
  * Implemented key events.
  * Implemnted mouse events.
  * Implemented several color models.
  * Added password field.
  * Implemented repeater connection support.
  * Changed from composited SurfaceView to ImageView which alleges less resource use and also yields built-in scaling.
  * Force orientation to landscape and prevent responding to configuration (orientation) changes. (configuration changes will reset the VNC connection because onCreate gets called).
  * Added protocol support for UltraVNC chat mode, although I did not write a chat UI.

**Usage Tips:**
  * In 1:1 scaling mode, use the DPAD center key/trackball down button to toggle between "panning" and "mouse control" modes.  Other DPAD keys will pan in "panning mode" OR will be cursor keys in "mouse control mode".

<br /><br />
