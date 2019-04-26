#Taken from tutorial at: http://victor.chahuneau.fr/notes/2012/07/03/kenlm.html
import sys
import nltk

for line in sys.stdin:
    for sentence in nltk.sent_tokenize(line):
        print(' '.join(nltk.word_tokenize(sentence)).lower())