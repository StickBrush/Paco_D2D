# Paco

1. Store some data into PACO: There is some created data. Uncommenting the code at the last line of onCreate of MainActivity before running the app, then these created data can be loaded into PACO when building the app.

2. Create Friend: From the menu, there is a Friends page. In Friends page, user can add friends by adding token(shows in the RECEIVE MESSAGE PAGE) and name. User also can see the list of their friends in this page.

3. Chenge Permission Setting: From the menu, there is a Permission Setting page. User can set user name, access level and other permission(keyhole) through this page.
(1) Access level: Access level can be set to 1, 2 or 3. Level 1 do not have any restriction on the request. As access Level set to 2 or 3, the requester can only get PoK from the successfully request instead of specific information of the data.
(2)Setting Level of Topics: In this block, uer can choose which level they want to put for each topic.
(3)Setting Permission of Levels: In this block, user can determine what information is needed to be put in the key of request of different level. For example, setting identity to stranger that means requester's identity will not be asked; however, requester's identity will be asked and checked when setting identity to Friend.
