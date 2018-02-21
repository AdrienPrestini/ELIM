const express = require('express')
const fs = require('fs');
const bodyParser = require('body-parser');
var GooglePlaces = require('google-places');
var cron = require('node-cron');

const app = express()
const port = 3000


const valMinNotifSemaine = 5;



//APPRENTISSAGE A FAIRE
/*var restaurant = "restaurant / asiatique / japonais / " + 
				"fast food / mc donald / burger king / subway / quick / five guys / " +
				"pizzeria / pizza mania / pizza hut / mister pizza / dominos pizza";

var supermarche = "supermarche / casino / intermarche / spar / auchan";

var coiffeur = "coiffeur / coiffure / salon rive gauche / salon de coiffure";

var bayes = new classifier.Bayesian({
	thresholds: {
      restaurant: 2,
      supermarche: 2,
      coiffeur: 2
    }
});

bayes.train(restaurant, 'restaurant');
bayes.train(supermarche, 'supermarche');
bayes.train(coiffeur, 'coiffeur');

//var bayes = new classifier.Bayesian();
var category1 = bayes.classify("manger fast food"); 
var category2 = bayes.classify("bigmac burger");  
var category3 = bayes.classify("pizza marguerite");  
var category4 = bayes.classify("mexicain");  


console.log("1-->"+category1);
console.log("2-->"+category2);
console.log("3-->"+category3);
console.log("4-->"+category4);*/

/*
	Date.getday():
	0-->Dimanche,
	1-->Lundi,
	2-->Mardi,
	3-->Mercredi,
	4-->Jeudi,
	5-->Vendredi,
	6-->Samedi,
*/

var places = new GooglePlaces('AIzaSyDwegDy_MQTVhX9XUOYAHTI4jFoqEFv_Ys');

var mysql      = require('mysql');
var connection = mysql.createConnection({
  host     : 'localhost',
  user     : 'root',
  password : '',
  database : 'elim',
});

connection.connect(function(err) {
  if (err) throw err;
  console.log("Connected!");
});

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());


app.get('/recherche', function (request, response) {
	var imei = request.query.imei

	var search = request.query.search

	var longitude = request.query.longitude

	var latitude = request.query.latitude

	var altitude = request.query.altitude

	//TODO: CHECK A QUELLE CATEGORIE CA CORRESPOND


	//S'IL N'EXISTE PAS ON DOIT DEMANDER POUR AJOUTER?????????

	var d = new Date();

	var sql = "INSERT INTO recherche (imei, motCherche, dateRecherche, heureRecherche, jourSemaine) VALUES ('0987654', 'fast food' , NOW(), NOW(), " + d.getDay() + ")";
	connection.query(sql, function (err, result) {
		if (err) throw err;
		console.log("Recherche ajoutee");
	});

	places.search({keyword: 'restaurant', location: [43.659486, 7.194246], radius: 200}, function(err, response) {
		console.log("search: ", response.results);
		response.send(response.results)
	});

	/*fs.writeFile("/search/"+imei+".txt", search, { flag: 'wx' }, function (err) {
	    if (err) throw err;
	    console.log("It's saved!");
	});*/

	//response.send(imei + "," + search)
})



//Routine par heure
cron.schedule('* */1 * * *', function(){
	
	var d = new Date();

	//Ajouter dans le where l'imei si on met plusieurs téléphone
	var sql = "SELECT motCherche, COUNT(motCherche) as nombreDeFois FROM recherche WHERE jourSemaine = " + d.getDay() + " AND heureRecherche BETWEEN '" +  d.getHours() + ":00:00' AND '" + (1+d.getHours()) + ":00:00'";
	connection.query(sql, function (err, result) {
		if (err) throw err;
		console.log(result);

		//DEMANDER POSITION USER

		for(var i = 0; i < result.length; i++){
			if(result[i].nombreDeFois >= valMinNotifSemaine){
				places.search({keyword: result[i].motCherche + '', location: [43.659486, 7.194246], radius: 200}, function(err, response) {
					console.log("search: ", response.results);
					response.send(response.results)
				});
			}
		}
	});
});

app.listen(port, (err) => {
	if (err) {
	return console.log('something bad happened', err)
}

console.log(`server is listening on ${port}`)
})


/*var d = new Date();
var n = d.getMinutes();

console.log(n);
toto = false;
//while(true){
	//var d = new Date();
	//var n = d.getMinutes();
	if(n == 45 && toto == false){
		console.log("ouiiii");
		toto = true;
		var sql = "INSERT INTO recherche (imei, dateRecherche, motCherche) VALUES ('0987654', NOW(), 'fast food')";
			connection.query(sql, function (err, result) {
			if (err) throw err;
			console.log("Recherche ajoutee");
		});
	}
//}*/


//app.post('/recherche', function(request, response) {
//	var imei = request.body.imei;
//	var mot = request.body.mot;
	//var latitude = request.body.latitude;
	//var longitude = request.body.longitude;
	//var altitude = request.body.altitude;

	//var informations = latitude + "/" + longitude + "/" + altitude;

//	console.log(mot);


	/*try {
		fs.appendFile("./positions/"+imei+".txt", ",\n"+informations, function (err) {
		  if (err) throw err;
		  console.log('Updated!');
		});
	}
	catch (e) {
		fs.writeFile("./positions/"+imei+".txt", informations, { flag: 'wx' }, function (err) {
		    if (err) throw err;
		    console.log("It's saved!");
		});
	}*/

//	response.send("lala");
//});






/*app.get('/position', function (request, response) {
	var imei = request.query.imei

	var position = request.query.position


	fs.writeFile("/position/"+imei+".txt", preferences, { flag: 'wx' }, function (err) {
	    if (err) throw err;
	    console.log("It's saved!");
	});

	response.send(imei+","+preferences)
})*/