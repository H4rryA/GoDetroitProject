# Go Detroit Project
This fullstack app was built at MHacks8 and provides a suite of features to make traveling safer and more reliable in Detroit.

## Mobile App
<img src="https://github.com/jordanwu97/GoDetroitProject/blob/master/frontend/screenshots/start.jpg" width="250">
<img src="https://github.com/jordanwu97/GoDetroitProject/blob/master/frontend/screenshots/routes.jpg" width="250">

## Key Features
* Safer routes for travelers
* Notify bus drivers which stops to be picked up and dropped off at
* Instantly send text to emergency contacts at the press of a button

### Safer Routes
Utilizing the open data portal for crime reports, a crime heat map of the Detroit Area was constructed with Google Maps Heat Map overlay and an api was built
to query a latitude and longitude for a relativistic crime rating. Using this data, we could give each route generated
by the Google Directions API a "crime rating". Using that rating, we rank each route by safety.

### Bus Driver Notifications
When a user selects one of the routes given by the app, it is logged to the server and the bus driver can see which routes
passengers need to get on or get off of. This data will guarantee that a bus driver does not skip bus stops where passengers
need to get off at or get on at.

### Emergency Contacts
The user can also set a list of emergency contacts to which, at the click of a button, send a text message with their latitude
and longitude, requesting help.


## Technologies Used
* Google Directions API
* Google Maps API
* Google Log-In API
* Detroit Open Data Portal
* Android
* Node.js
* MongoDB
