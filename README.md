# ElectricPoetry
Poetry generator based on human-created texts.
Poems can be based on texts by parsing in a text file using the PoemParser class, and parsing a language model based on that text into LanguageModelParser.

## Language Model Generation
Language models have been generated using the tutorial at http://victor.chahuneau.fr/notes/2012/07/03/kenlm.html 

## Restoring MongoDB
A mongodump of the database can be found in directory 'dump' directory. Use the mongodb command 'mongorestore' to use the database
https://docs.mongodb.com/manual/reference/program/mongorestore/

## Running the program
Poems can be run by opening the project in Eclipse (or an IDE of your choice), and running the 'PoemGeneratorEA' class. More information about configuration of the algortihm can be found in the javadocs.
