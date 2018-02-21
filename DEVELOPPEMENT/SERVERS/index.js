const express = require('express')
const fs = require('fs');
const bodyParser = require('body-parser');
var GooglePlaces = require('google-places');
var cron = require('node-cron');
var BayesClassifier = require('bayes-classifier')

const app = express()
const port = 3000

const valMinNotifSemaine = 5;



/**
 * Apprentissage avec le classifier
**/
var classifier = new BayesClassifier()

var othersWords = [
  "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu",
  "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu", "inconnu"
]

var restaurantWords = [
  "restaurant", "asiatique", "japonais", "fast food", "mc donald", "burger king", "subway", "quick", "five guys",
  "pizzeria", "pizza mania", "pizza hut", "mister pizza", "dominos pizza"
]
 
var supermarcheWords = [
  "supermarche", "casino", "intermarche", "spar", "auchan"
]

var coiffeurWords = [
  "coiffeur", "coiffure", "salon rive gauche", "salon de coiffure"
]

var boulangerieWords = [
  "boulangerie", "pain en chocolat", "croissant", "baguette"
]

classifier.addDocuments(othersWords, `inconnu`) 
classifier.addDocuments(restaurantWords, `restaurant`)
classifier.addDocuments(supermarcheWords, `supermarche`)
classifier.addDocuments(coiffeurWords, `coiffeur`)
classifier.addDocuments(boulangerieWords, `boulangerie`)
 
classifier.train()


//console.log(classifier.getClassifications("manger fast food"))
//console.log(classifier.getClassifications("bigmac burger"))
//console.log(classifier.getClassifications("pizza marguerite")) 
//console.log(classifier.getClassifications("casino"))
//console.log(classifier.getClassifications("auchan drive"))
//console.log(classifier.classify("auchan drive"))

//console.log("\n\n\n" + classifier.docs[36]["value"])

//supermarcheWords.push("auchan drive");
//classifier.addDocuments(supermarcheWords, `supermarche`)
//classifier.train()

//console.log(classifier.getClassifications("drive"))



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
  port : 3306
});

connection.connect(function(err) {
  if (err) throw err;
  console.log("Connected!");
});

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());


app.post('/recherche', function (request, response) {

	var imei = request.body.imei

	var search = request.body.search

	var longitude = parseFloat(request.body.longitude)

	var latitude = parseFloat(request.body.latitude)

	console.log("Appelle de recherche avec " + search);

	places.search({keyword: search + '', location: [latitude, longitude], radius: 1000}, function(err, res) {
		console.log("search: ", res.results);
		if((res.results).length > 0){
			response.send(res.results);

			var resClassifier = classifier.classify(search + '') + "";

			if(resClassifier == "inconnu"){
				resClassifier = search + "";
				var ajoutCategorie = [ search + "" ];
				classifier.addDocuments(ajoutCategorie, search + "")
				classifier.train()
			} else {
				var exist = 0;
				for(var i = 0; i < classifier.docs.length; i++){
					if(classifier.docs[i]["value"] == search){
						exist = 1;
					}
				}
				if(exist == 0){
					classifier.addDocument(search + "", resClassifier)
					classifier.addDocument("inconnu", "inconnu")
				}
			}

			var d = new Date();

			var sql = "INSERT INTO recherche (imei, motCherche, dateRecherche, heureRecherche, jourSemaine) VALUES ('" + imei + "', '" + search + "', NOW(), NOW(), " + d.getDay() + ")";
			connection.query(sql, function (err, result) {
				if (err) throw err;
				console.log("Ajout de " + search + " pour l'imei " + imei + " dans la base de donnee.\n");
			});
		} else {
			response.send("[{}]");
		}
	});
})


app.post('/routine', function (request, response) {
	var d = new Date();
	console.log("routine");
	var imei = request.body.imei

	var longitude = request.body.longitude

	var latitude = request.body.latitude


	var heureDebut = '';

	var heureFin = '';

	if(d.getHours() == 0){
		heureDebut = '23:30:00';
		heureFin = '00:30:00';
	} else {
		heureDebut = (-1 + d.getHours()) + ':30:00';
		heureFin = d.getHours() + ':30:00';
	}


	//Ajouter dans le where l'imei si on met plusieurs téléphone
	//SELECT motCherche, COUNT(motCherche) as nombreDeFois FROM recherche WHERE jourSemaine = 3 AND heureRecherche BETWEEN '23:30:00' AND '0:30:00'
	//SELECT motCherche, COUNT(motCherche) as nombreDeFois FROM recherche WHERE jourSemaine = 3 AND heureRecherche BETWEEN '8:30:00' AND '9:30:00' GROUP BY motCherche ORDER BY nombreDeFois desc limit 1
	var sql = "SELECT motCherche, COUNT(motCherche) as nombreDeFois FROM recherche WHERE jourSemaine = " + d.getDay() + " AND heureRecherche BETWEEN '" +  heureDebut + "' AND '" + heureFin + "' GROUP BY motCherche ORDER BY nombreDeFois desc limit 1";
	connection.query(sql, function (err, result) {
		if (err) throw err;
		console.log(result);

		if(result.length > 0){
			if(result[0].nombreDeFois >= valMinNotifSemaine){
				places.search({keyword: result[0].motCherche + '', location: [latitude, longitude], radius: 1000}, function(err, res) {
					console.log("search: ", res.results);
					response.send(res.results)
				});
			}
		} else {
			response.send("[{}]");
		}
	});

})


app.post('/rechercheAncienne', function (request, response) {
	var imei = request.body.imei

	var search = request.body.search

	var longitude = request.body.longitude

	var latitude = request.body.latitude

	var date = request.body.date

	var temps = request.body.temps

	var d = new Date(date + " " + temps);

	places.search({keyword: search + '', location: [latitude, longitude], radius: 1000}, function(err, res) {
		if((res.results).length > 0){
			var resClassifier = classifier.classify(search + '') + "";

			if(resClassifier == "inconnu"){
				resClassifier = search + "";
				var ajoutCategorie = [ search + "" ];
				classifier.addDocuments(ajoutCategorie, search + "")
				classifier.train()
			} else {
				var exist = 0;
				for(var i = 0; i < classifier.docs.length; i++){
					if(classifier.docs[i]["value"] == search){
						exist = 1;
					}
				}
				if(exist == 0){
					classifier.addDocument(search + "", resClassifier)
					classifier.addDocument("inconnu", "inconnu")
				}
			}

			var sql = "INSERT INTO recherche (imei, motCherche, dateRecherche, heureRecherche, jourSemaine) VALUES ('" + imei + "', '" + search + "', " + date + ", " + temps + ", " + d.getDay() + ")";
			connection.query(sql, function (err, result) {
				if (err) throw err;
				console.log("Ajout de " + search + " pour l'imei " + imei + " dans la base de donnee.\n");
			});
		}
	});
})

app.listen(port, (err) => {
	if (err) {
	return console.log('something bad happened', err)
}

console.log(`server is listening on ${port}`)
})

