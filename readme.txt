
					Mobile Computing and the Internet of Things
								Coursework 2017
						Samir Ghimire, Christos Aleiferis
						
	Github URL:	https://github.com/chris91/mucproject
	
	MUCProject App:

		MUCProject is an Android app for indoor location logging using IndoorAtlas 2.3 SDK. 
		The application consists of an activity called MUCActivity in which we implement 
		the geo logging, geo fencing and also provide a map interface showing the current location.
		
		The app requests for a location update every second. After each location update a Position 
		object is created (Position class contains the latitude, longitude and also has a method 
		to compute the geographical distance between two locations).
		
		Five points of interest have been defined inside the mapped area and every time the location
		is updated the application checks if the current position is within 3 meters of any of these points.
		In that case a toast with the location's name appears. The Position objects created after
		each iteration are also saved in Firebase.
		
		BlueDotView class from the IndoorAtlas Sample application has been used in the application
		to create the dot which represents current position in the floorplan.
		
		MUCProject is tested on Android 5.0.1