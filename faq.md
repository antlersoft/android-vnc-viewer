= Frequently Asked Questions =

## 0. What is VNC? ##

> VNC is an open, more-or-less standard protocol for controlling computers
> (display, mouse and keyboard) from other computers over a network.  It
> is most useful if you have a computer that's always on (a server)
> that you want to control from its ordinary desktop interface.  We
> can't explain everything about VNC here.  If you want to use **android-vnc-viewer**, it's best to first set up so you can control
> your server from another PC client; then you'll know how you need
> to configure the phone as a client.  Please refer to the following
> instructions (these aren't ours):

  * For PC's: [TightVNC setup](http://www.tightvnc.com/winst.html)
  * For Mac's: [Apple remote desktop sharing](http://www.macminicolo.net/Mac_VNC_tutor.html)

## 1. I can run it fine over wifi-- how do I get it to work over my carrier network? ##

> android-vnc-viewer works fine over 3g and Edge networks (Edge can be slow-- consider a smaller bit depth).  You need to set the Address field to the _public internet address_ of the PC you're trying to control.  If the PC you're connecting to accesses the internet through a router, this will be the WAN address assigned to the router by your ISP; you'll also need to forward the VNC port (5900) from the router to your PC (exactly how you do this depends on the details of your router, so I can't give more explicit instructions here).

> If your PC is on a network separated from the public internet by systems you can't control (it's on a corporate network), you might not be able to use android-vnc-server to control it from your carrier network.  You might have to use one of the commercial Android products (like PhoneMyPC) that support connections over their own public servers.

> See also [How do I connect to VNC from 3G without opening an insecure port to the internet?](#9..md)

## 2. I'm having trouble with connecting to Apple Remote Desktop on OS/X. ##

> The **android-vnc-viewer**
> does connect to Apple Remote Desktop on OS/X.  The default color
> depth (64-colors) does not work, so you will have to configure
> for 24-bit color before you connect.

> You will also need to turn on "Local mouse pointer".

> You'll probably also need to check "Force full-screen bitmap".  Unfortunately, this may result in android-vnc-viewer running out of memory and crashing on hi-res Mac screens (the Droid and Nexus can handle higher-res screens than older phones).  See also [Why am I seeing green?](#3..md)

## 3. Why am I seeing green? ##

> Working with large PC screens can require more RAM than Android
> allows any one app to have.  To work around this, android-vnc-viewer breaks
> large PC screens into smaller chunks that will fit into its memory.
> In normal operation you will see a patch of green that indicates
> that an update from the server has overlapped a tile and needs
> to be retransmitted.  This
> works very well with some VNC servers and less well with others; in
> particular the built-in VNCs for OS/X and Ubuntu won't always retransmit
> sections of the screen on request, leaving you with a green screen (or
> partial screen) on your phone that doesn't update.

> One fix for this is to check "Force full-screen bitmap" on the configuration screen; however, this might cause android-vnc-viewer to run out of memory and force-close.  Other options are to use a different VNC server (x11vnc is
> recommended for Linux users) or to set your PC to use a lower resolution.

> I hope to have a better fix for the screen resolution/phone memory problem
> in the future.

## 4. I can't see the mouse cursor! ##

> Some VNC servers (such as OS/X's built-in one) require the client to render the mouse cursor.  VNC can render a simple cursor in these cases; on the connection configuration screen, select "Local mouse pointer".  The mouse cursor will show up
> as a small dot.

## 5. I can't log in to Real VNC Enterprise ##

> Real VNC Enterprise can use a proprietary encryption protocol over the
> standard VNC protocol.  Android VNC (and as far as I know, all other
> non-RealVNC clients) can't use this protocol.  The work-around is to
> configure the Real VNC server to use None or VNC authorization
> instead of Windows authorization; then it will use the standard
> and open protocol; you should use SSH or a VPN as an alternative
> source of encryption.  Other than that, you can politely ask
> RealVNC to supply an Android version of their client :)

## 6. How do I right click, or send special keys? ##

> See the [User Manual](Documentation.md).
> Hold down the camera button while clicking to
> do a right-click; in the default control mode double-clicking is also
> interpreted as a right-click.

> Support for sending just about any special, Ctrl or Alt
> key is available from the **[Send Keys](Documentation#Special_Keys.md)** menu item.  You can also use this to send mouse clicks that are difficult to send directly, like middle-button clicks or right clicks when your phone doesn't have a camera button.
> Ctrl-Alt-Del is available as its own
> menu function.

## 7. How do I double-click? ##

> In the default [input mode](Documentation#Input_Modes.md),
> a double tap is interpreted as a right-click.  To actually send a
> double click, you have a couple of different options:

  * You can double click the trackball or D-Pad center
  * You can use switch to another input mode such as D-Pad Pan; Touch Mouse or Mouse Control Mode where a double tap is interpreted as a double-click.

## 8. How do I get it to work with my repeater? ##

> Put the address and port of the repeater server (repeater port is typically 5901) in the configuration.  Then put the behind-the-repeater host and port in the repeater info, which you access by pressing the _Repeater_ button
> on the configuration screen (scroll down to see it...)

## 9. How do I connect to VNC from 3G without opening an insecure port to the internet? ##

> We recommend you tunnel your VNC connection over SSH with
> [connectbot](http://code.google.com/p/connectbot).  Set up
> your connectbot connection to the server hosting your
> VNC server, and configure a tunnel to forward port 5900.
> Then you can set up an android-vnc-viewer connection to
> 5900 on localhost.

## 10. Why can't I connect? ##

> There are a number of reasons a connection to your
> VNC server might fail.

  * You are using an unsupported authorization protocol (see [I can't log in to Real VNC Enterprise](#5..md)).

  * Your phone can't connect to the address you specified for the connection.  Try connecting to another service (i.e. ssh) at that address to verify connectivity.  Remember that connectivity might be different if you are accessing the server through you carrier's network rather than over a local Wi-Fi network.

  * Your password might be entered wrong.

  * Your VNC server might not support the color depth you requested.  Try a different color depth (24-bit).

  * Your phone might be running out of memory RAM.  If **Force full-screen bitmap** is selected, de-select it.

  * We've noticed that sometimes connections just seems to fail for a few minutes after you diconnect; we're not sure why.  Sometimes just waiting will make your server available again.

> If none of these suggestions work, you might have run into a bug in android-vnc-viewer.  Look at the submitted issues to see
> if someone has encountered a similar problem.
> You can help the project by submitting a bug report.
> If you have access to the Android SDK, you might want
> to include LogCat output from the time
> you attempted your connection in your bug report.

> In future versions of the code we'll try to be more specific in
> the error messages about what's gone wrong.

## 11. My phone doesn't have a keyboard or buttons that Android VNC expects!  What do I do? ##
> You can use the [Send Keys](Documentation#Special_Keys.md) menu item to send just about any keystroke or mouse click that you want.  It might not be convenient, but it will get the job done.

> There is a menu item _Send Text_ for phones that lack physical keyboards that will provide a text box where you can bring up the soft keyboard, type some text, and then have that text sent to the phone.  This should support all the keys on the soft keyboard, including the Enter key, and will work with phones where you can't pull up the keyboard with a long press on Menu.

> If you have a phone with other needs that Android VNC doesn't cover, please submit (or add to an existing) bug report on the issues page.

## 12. Should I install from this site or Android Market? ##

> Android Market will have the latest version of the code
> we consider stable.
> You should always be able to upgrade the version in Market without
> losing your settings.  If you want to help get the bugs
> out and to have the very latest bleeding edge version, download
> development versions from here in Google Code.  We'll try
> and allow upgrades with the development version.  The development versions
> are now certificate-compatible with the Market versions, so
> you won't have to uninstall to go from one to the other.

## 13. It says the installation failed, and now I can't even access the old version! ##

> This problem should only happen with very old versions of the software.  Yo may need toUninstall the android-vnc-viewer before installing
> a new development version.  Upgrade installations may not work.  An
> upgrade installation that fails will leave you unable to run the old
> version or install a new one-- We've found that if you re-install the
> old version you had, you can then uninstall it properly and then
> install the new version.  You may need to reboot your phone before
> you can do the reinstall.