= Using android-vnc-viewer =

**android-vnc-viewer** lets you use your Android mobile device as a client for a VNC server.  It is a work in progress and still has a number of limitations.

## Connection Configuration ##
When you first open **android-vnc-viewer**, you will see a connection configuration page.  Here you can set up a connection with a VNC server, or choose an already configured connection.

The page is divided into two sections.  The top section contains a drop-down list of the connection configurations you've created.  The first time you run android-vnc-viewer, this list will have only one entry: _New_.  Select _New_ when you want to create a new configuration.

Next to the list is a _Connect_ button.  The _Connect_ button will start the VNC client with the currently selected configuration.

The bottom section of the page is where you enter your connection configurations.  There are more options than will fit on the page, so this section scrolls.

![http://sites.google.com/site/androidvncviewer/home/config.png](http://sites.google.com/site/androidvncviewer/home/config.png)

> _**Nickname**_ Each configuration you create can have a nickname which will make it easy to find in the list.  Enter that name here.

> _**Password**_ If your VNC server is configured to require a password, enter that password here.  Most configuration settings are stored in a simple database on your device.  The password will only be stored if you check _Keep_; otherwise, you will have to re-enter it when you recall a configuration.  Even if the password is not stored in the database, it will remain available on the page for as long as **android-vnc-viewer** runs on your device.

> _**Address**_ This is where you enter the DNS name or IP address of the computer running the VNC server to which you want to connect.

![http://sites.google.com/site/androidvncviewer/home/config2.png](http://sites.google.com/site/androidvncviewer/home/config2.png)
> _**Port**_ Here you should enter the port number of the VNC server.  This field will default to the first VNC port number, 5900.

> _**Color Format**_ The client supports a number of color formats, which are specified by number of colors/number of bits per pixel.  Formats with more bits per pixel provide greater fidelity to high color depth displays, but use more bandwidth (sometimes dramatically more because they don't compress as well) and more CPU on your Android device.  Not all VNC servers support all color depths; OS/X Remote Desktop for example requires 24-bit color.  If you are having trouble connecting, try another color depth.

> _**Use local mouse pointer**_ Some VNC servers (notably OS/X desktop sharing) will not draw the mouse pointer on the client; it's hard to use them without knowing where the mouse is.  Checking this option will cause **android-vnc-viewer** to draw a small square cursor at the mouse position (it's not the actual mouse cursor, but it's better than nothing).

> _**Force full-screen bitmap**_ The VNC client requires a bitmap for it's representation of the display.  For a VNC server serving a large display, this bitmap might require more memory than Android allows in an application.  To work with these large displays, **android-vnc-client** breaks large display into smaller tiles and works with only one tile at a time.  Unfortunately, this will sometimes create some visual artifacts.  In particular, when a large display is in [Fit to Screen](#Scaling.md) mode only a portion of it will be visible, and in [1:1](#Scaling.md) mode as you pan over a large display you might see different parts of the display flicker in.  **android-vnc-client** will only use tiling if it thinks the display is too large to fit in memory, but you can override this decision and always turn off tiling by checking **_Force full-screen bitmap_**.  This might cause the application to crash on connection.

> _**Repeater**_ Press this button if you are using an UltraVNC repeater to
> connect to your VNC server.  In this case, you should have entered the
> address and the port of the _repeater_ in those fields; in the repeater dialog you should enter the VNC server address from the repeater and the screen id or port number, separated by colons.

The menu on this page allows you to _Delete_ unneeded connection configurations;
to _Save as Copy_ an existing connection to base a different configuration from it; or to open this _Manual/Wiki_.

![http://sites.google.com/site/androidvncviewer/home/config%20menu.png](http://sites.google.com/site/androidvncviewer/home/config%20menu.png)
## VNC Client ##

After you press the connect button, **android-vnc-viewer** will try to connect to the specified VNC server.  If it is able to handshake, authenticate and download the first frame, you will be able to see and control the served display.

In general, VNC servers expect clients with a full keyboard, a mouse and a
generously-sized screen.  Because the Android device lacks these,
**android-vnc-client** has some special adaptations to make it usable.

![http://sites.google.com/site/androidvncviewer/home/connectmenu.png](http://sites.google.com/site/androidvncviewer/home/connectmenu.png)
### Scaling ###

The served display can be shown on the Android device in three modes.  You
can switch between the modes with the _Scaling_ (Menu-Z) menu item.

> _Zoomable_ is the default mode with the latest version of **android-vnc-viewer**.  It allows you to select one of a number of zoom levels on the fly, so you can see more of your screen or zoom in for more accurate touch control.  To some extent it supercedes the other modes, but it will use slightly more battery power.

> _1:1_ mode shows the display with pixels mapped one-to-one.  Typically, only a portion of the served display will show on the Android device; you can pan around the display with the device controls.

> _Fit to Screen_ mode scales the full display so it will fit on the device screen.  This doesn't work properly with large displays, since the phone can't fit them all into its memory.

### Input Modes ###

There are several modes for adapting the input controls of the Android device
to the VNC display, to enable a style of interaction most convenient for the applications
you are using.
You can switch between these modes with the _Input Mode_ (Menu-P) menu item.

> _Touch Mouse Pan and Zoom_ This is the default input mode and is designed to work like the Android browser.  You can both pan the display and control the mouse using the touchscreen and gestures. You pan by dragging or flicking on the touchscreen; you click the mouse by tapping on it. You right-click by double-tapping (or by holding down the camera button while tapping). You drag the mouse by doing a long press on the display, and then dragging.  In this mode the trackball or DPad (if your phone has one) can also be used to control the mouse; this may give you finer control.  You can zoom the screen size with the +/- buttons, or, if your device supports multi-touch and has Android 2.0+, you can pinch to zoom out and spread to zoom in.

> _Touchpad Mode_ This is the alternate gesture-based interface.  In this mode, touching the screen moves the mouse cursor like a mouse touchpad.  The screen will pan to follow the mouse (unless you turn it off).  Tapping the screen clicks the mouse, where it is rather than where you tap.  For multi-touch capable devices, drag two fingers to pan the screen independent of the mouse position.  Otherwise, it works like Touch Mouse Pan and Zoom.

> _No Pan; Trackball Mouse_ This mode is only available in [Fit to Screen](#Scaling.md) scaling and is the only input mode available then.  In this mode the touchscreen is not used.  Keyboard events are sent to the server and the trackball (if your device, like the G1, has a trackball) controls the VNC mouse.

> _Desktop Panning Mode_ In this mode, both the touchscreen and the trackball are used to pan the device display over the larger VNC display.  Keyboard events are sent to the server.  Pressing the trackball toggles between _Desktop Panning_ and _Mouse Pointer Control_ modes.

> _Mouse Control Mode_ In this mode, use the touchscreen to control the mouse.  Touching the screen generates a mouse click at that point; dragging on the screen creates a mouse drag.  Keyboard events are sent as normal.  The trackball is used to send arrow-key events to the VNC server.  Pressing the trackball toggles between _Mouse Pointer Control_ and _Desktop Panning_ modes.

> _Touch Pan; Trackball Mouse_ In this mode, drag on the touchscreen to pan the device display over the VNC display.  Keyboard events are sent to the server.  The trackball controls the VNC mouse.  Pressing the trackball sends a mouse click; holding the ball down while rolling accomplishes a click and drag.  This is the default input mode when scaling is set to One-to-One.

> _DPad Pan; Touch Mouse_ In this mode, use the directional pad (available on some devices) to pan the display over the VNC display.  Touch the screen to send a mouse click; touch and slide to send a mouse drag.  Use the camera button while touching the screen to simulate a right-button click or drag.

### Special Controls ###

Some special controls try to make up for missing features of an Android
device (i.e., a G1) as a VNC controller.

  * To use the right mouse button, hold down the **Camera** button while clicking as appropriate for the selected input mode (touching the screen or clicking the trackball).  If your phone has no dedicated camera button, you can send a right click by double-tapping or with the [\_Send Keys\_](#Special_Keys.md) menu item.

  * To send the _Esc_ key, press the **Back** button on the device.

  * To send the arrow keys, roll the trackball in _Mouse Pointer Control Mode_ or use Menu-H,J,K,L for Left, Down, Up, Right

  * The Alt button generally is used to send the special symbols that aren't otherwise available on the keyboard, rather than sending the Alt meta-key over the VNC connection.

  * The volume control buttons simulate turning the mouse scroll wheel up or down.  (Holding them down doesn't work, to scroll further click repeatedly).

  * To send control keys, function keys and other special keys and combinations see [Special Keys](#Special_Keys.md).

  * To send (or resend) a whole block of text, use the Send Text menu option.

### Other Menu Items ###

> _Send Keys_ (Menu-S) This opens the [Special Keys](#Special_Keys.md) dialog to let you configure and send special keys and combinations.

> _Mouse @_ (Menu-M) Warps the VNC mouse to the center of the portion of the display shown on your device.

> _Send Text_ (Menu-E) Opens a dialog that lets you enter a block of text that can then be sent to the server.  Useful for phones without a physical keyboard.

> _Color Mode_ (Menu-C) Allows you to switch the [Color Mode](#Connection_Configuration.md) of your connection.

![http://sites.google.com/site/androidvncviewer/home/connectmenu2.png](http://sites.google.com/site/androidvncviewer/home/connectmenu2.png)
> _Pan Follows Mouse_ (Menu-F) The display will automatically pan to follow the mouse around the screen

> _Mouse follow pan_ If you pan the display so the mouse moves off the visible portion of the screen, the mouse will be warped to the middle of the screen.  You can use both follow modes together.

> _Disconnect_ (Menu-D) Closes the current VNC connection, returning you to the configuration page.

> _Ctrl-Alt-Del_ (Menu-A) Sends Ctrl-Alt-Del over the VNC connection.

> _Info_ (Menu-I) Gives information about current configuration

> _Send Key Again_ (Menu-G) Send the last sent [special key](#Special_Keys.md) again

## Special Keys ##

![http://sites.google.com/site/androidvncviewer/home/specialkeys.png](http://sites.google.com/site/androidvncviewer/home/specialkeys.png)
The _Send Keys_ (Menu-S) button opens the **Send Special Keys** dialog.  This allows you to access all the keys on a full-sized keyboard, or to send keys or any mouse button with any combination of the modifiers Shift, Alt and Ctrl.  (You might even use it as a touch-screen keyboard, but you would have to
be incredibly patient.)

The dialog has checkboxes to select Shift, Alt and Ctrl, and a list of keys to select from.  When you have the desired
key combination, press the Send button and it will be
sent to the VNC server.

Every time you send a special key in this way it is saved to a list.  As a shortcut you can select from that list instead of selecting from the list of all the keys.

Actually, multiple lists of special keys are supported.  You can use the buttons at the bottom of the scrolling section of the
dialog to create a New list or to Copy your list to a new
name.  This way you can have compact, application specific
lists, if you want, for quick access to the keys you need.

An entry on the [VNC Client](#VNC_Client.md) menu lets you re-send the
last special key you sent with the shortcut Menu-G.

You can operate this dialog from the keyboard as well as
using the touch controls.  Press Alt and Shift to toggle
those modifiers.  Press the Search button to toggle the
Ctrl modifier.  When you type a regular key on the keyboard,
that key plus the selected modifiers will be sent
immediately to the server.

## Devices without Keyboards ##

android-vnc-viewer can work effectively on devices without physical keyboards.  Use the Send Keys menu option to send keystrokes that are commands in themselves (i.e. Alt-Tab).  When you need to enter a block of text, use the Enter Text command, type the text with the Android keyboard, and press send.  You can edit the text before it is sent to make sure it is correct, and
the Enter Text command will remember the texts you've sent in that session, so you can send them again quickly (convenient for passwords you have to enter many times).

## Shortcuts ##

android-vnc-viewer lets you create home screen shortcuts for quick access to
your favorite connections.  Long click on the home screen, select Shortcuts in the menu, then scroll down to VNC Connection.  You'll get a page that lists
your configured connections.  Select one and it will be added to the home screen as a short cut.  Selecting a short cut starts that connection immediately.