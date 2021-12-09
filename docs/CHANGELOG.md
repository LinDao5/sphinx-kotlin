# ChangeLog

# Version 1.0.0-alpha35 (2021-12-03)
 - Fixes crash on Android 12 (API 31) for tor accounts
 - Improves feed items order
 - Add loading wheel while loading videos  

# Version 1.0.0-alpha34 (2021-11-27)
 - Adds newsletter content on feed
 - Adds video content on feed
 - Adds podcast search
 - Adds people site integration for tor accounts

# Version 1.0.0-alpha33 (2021-11-12)
 - Adds tabbed dashboard and new feed section

# Version 1.0.0-alpha32 (2021-11-03)
 - Fixes crash on Android 12 (API 31)

# Version 1.0.0-alpha31 (2021-10-30)
 - Adds ability to record and send video from Camera
 - Fixes paid content issue

# Version 1.0.0-alpha30 (2021-10-22)
 - Adds ability to send video from Library
 - Adds ability to search for contacts and chats on Dashboard
 - Adds ability to search for contacts on Contacts section
 - Adds ability to see contact details and edit from Contacts section
 - Adds some Japanese translations
 - Adds ability to set custom meeting server

# Version 1.0.0-alpha29 (2021-10-14)
 - Adds ability to receive video messages and paid video messages
 - Adds ability to watch for received video messages on full screen view
 - Adds some missing Japanese translations
 - Adds version label on left menu
 - Improves Podcast loading process and prevents crashes

# Version 1.0.0-alpha28 (2021-10-11)
 - Adds ability to record and send audio messages
 - Adds new onboard screens
 - Adds pull down to refresh on dashboard
 - Fixes Podcast tag on tribes
 - Fixes hiding CREATE TRIBE button on virtual nodes

## Version 1.0.0-alpha27 (2021-10-08)
 - Adds ability to record and send audio messages
 - Adds new onboard screens
 - Adds pull down to refresh on dashboard
 - Fixes Podcast tag on tribes
 - Fixes hiding CREATE TRIBE button on virtual nodes

## Version 1.0.0-alpha26 (2021-09-30)
 - Adds chat invoices

## Version 1.0.0-alpha25 (2021-09-24)
 - Adds ability to receive Audio Messages
 - Adds Payment Template functionality

## Version 1.0.0-alpha24 (2021-09-17)
 - Adds ability to reply to Bot responses
 - Selected message menu improvements
 - Adds variability of message bubble size depending on content
 - Message Boost UI improvements

## Version 1.0.0-alpha23 (2021-09-10)
 - Adds message grouping
 - Adds ability to send/receive paid text messages
 - Fixes for paid images

## Version 1.0.0-alpha22 (2021-09-02)
 - Adds ability to send an image with a price
 - Adds ability to receive and pay for an image with a price
 - Adds swipe to close feature when viewing full screen images
 - Adds ability to create and modify subscriptions (recurring payments) for contacts

## Version 1.0.0-alpha21 (2021-08-27)
 - Improvements to loading of messages that contain Images
 - Improvements to Save Image functionality
 - Adds ability to Edit a contact's details

## Version 1.0.0-alpha20 (2021-08-25)
 - Fixes to podcast functionality within the Tribe Chat screen
 - Adds ability to view Images in full screen mode
 - Adds ability to save images to gallery
 - Adds ability to add a contact for a price

## Version 1.0.0-alpha19 (2021-08-19)
 - Fixes issue with onboard where data was being persisted prior to successful token
   generation network request
 - Adds Japanese and Chinese translations
 - Fixes string resource formatting issues
 - Changes chat mute/unmute functionality to now switch immediately instead of waiting
   for network response (will be set back upon network error)
 - Fixes key export backup by removing formatting (now is a single line)
 - Adds external authorization handling
 - Adds persistence of color values for users/chats

## Version 1.0.0-alpha18 (2021-08-13)
 - Adds ability for Tribe Admin to view members, their status', and remove members
 - Fixes issues related to long press action on messages
 - Fixes issues related to link previews

## Version 1.0.0-alpha17 (2021-08-11)
 - Adds ability to resend failed messages
 - Adds previews for
     - Shared Contacts
     - Shared Tribes
     - Http urls
 - Adds capability to now see tribe bot messages
 - Fixes bubble long click listener
 - Fixes some issues related to sending of attachments

## Version 1.0.0-alpha16 (2021-08-06)
 - Adds ability to boost podcast content creators from the Tribe Chat and Podcast screen
 - Adds limited functionality for changing the RelayUrl from Profile screen
     - Changing it to an onion address if Tor is not running currently not supported
 - Fixes bug that was joining a tribe twice if share tribe link was scanned from the dashboard

## Version 1.0.0-alpha15 (2021-08-04)
 - Adds link detections to messages
     - Urls
     - Share Tribe Links
     - Share Contact (Lightning Node Public Key) links
 - Adds Jitsi Calls
 - Adds check for new app versions
 - Adds support for scanning of new contact links when utilizing the scanner from the dashboard
 - Bumps Tor version from `0.4.6.2-alpha` -> `0.4.6.5`

## Version 1.0.0-alpha14 (2021-07-30)
 - Adds ability to approve/deny member join requests in Tribes
 - Fixes bug that was attempting to fetch podcast feed when url was empty
 - Refactors the On Board process
     - User picks up where they left off if initial on boarding was incomplete.
     - Imported RelayUrl with a scheme of `http` requires user confirmation.
     - Sensitive information temporarily stored until after a user generates their private/public
       key pair now uses Encrypted Shared Preferences.

## Version 1.0.0-alpha13 (2021-07-28)
 - Adds ability to edit a Tribe
 - Displays Podcast Boosts (instead of showing the raw json)
 - Spanish Translation

## Version 1.0.0-alpha12 (2021-07-23)
 - Adds ability to create a Tribe
 - Adds ability to edit Tribe details
 - Fixes Time/Date formatting issues

## Version 1.0.0-alpha11 (2021-07-21)
 - Adds ability to change and stream Sats to Podcast creators
 - Adds ability to scan Lightning invoices from the dashboard

## Version 1.0.0-alpha10 (2021-07-16)
 - Adds ability to create Lightning Invoices for receiving payments
 - Adds ability to view Lightning Transaction history
 - Adds ability to change Profile Picture
 - Updates dependencies to latest stable versions
 - Fixes how direct payment's containing images are shown in chats 
 - Fixes Chat Message Input Scroll View

## Version 1.0.0-alpha09 (2021-07-13)
 - Adds Giphy integration to chats
 - Adds ability to delete messages
 - Adds Support Ticket detail screen
 - Documentation updates
 - Removes notification of 200 message limit for large chats
     - Note: Chats are still limited to 200 messages

## Version 1.0.0-alpha08 (2021-07-10)
 - Fixes generation of Relay token

## Version 1.0.0-alpha07 (2021-07-08)
 - Adds ability to invite a new user
 - Adds ability to send images from device gallery
 - Adds ability to exit a tribe
 - Refactors the chat menu out of a detail screen, and moves functionality into the chat screen
 - Camera improvements

## Version 1.0.0-alpha06 (2021-07-02)
 - Adds sending payments:
     - From a Chat with a Contact via the footer menu options
     - From the Dashboard via scanning or entering a Lightning Node Public Key
 - Adds frameworks for encrypting and uploading attachments to the meme server
 - Adds ability to take pictures and send
 - Adds ability to copy message text via the long press pop up menu
 - Adds ability to reply to messages via the long press pop up menu
 - Fixes bug related to FirebaseMessaging whereby token retrieval failure was stalling network requests
 - Fixes crash related to Message Selection Menu when application was cycled to background/foreground

## Version 1.0.0-alpha04 (2021-06-25)
 - Adds ability to receive images
 - Adds ability to send direct payments
 - Fixes crash related to Message Selection Menu blurred background

## Version 1.0.0-alpha03 (2021-06-16)
 - Adds Message Selection Menu functionality with limited functionality
     - Boosting message only
 - Fixes podcast player stopping after device enters doze mode

## Version 1.0.0-alpha02 (2021-06-16)
 - Adds Podcasting with limited functionality
     - Streaming Sats turned off

## Version 1.0.0-alpha01 (2021-06-04)
 - Initial Release