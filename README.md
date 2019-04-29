# ElectricPoetry
Romantic poetry generator based on the novel 'Do Androids Dream of Electric Sheep' by Philip K Dick.
Poems can be based on other texts by parsing in the file using PoemParser, and parsing a language model based on that text into LanguageModelParser.

## Language Model Generation
Language models have been generated using the tutorial at http://victor.chahuneau.fr/notes/2012/07/03/kenlm.html 

## Restoring MongoDB
A mongodump of the database can be found in directory 'dump' directory. Use the mongodb command 'mongorestore' to use the database
https://docs.mongodb.com/manual/reference/program/mongorestore/

## Running the program
Poems can be run by opening the project in Eclipse (or an IDE of your choice), and running the 'PoemGeneratorEA' class. More information about configuration of the algortihm can be found in the javadocs.
