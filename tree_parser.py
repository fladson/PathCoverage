from itertools import takewhile
import sys
import inspect

is_tab = '>'.__eq__

out = open("resultado.txt", "w")

def build_tree(lines, query):
    lines = iter(lines)
    stack = []
    count = 0
    for line in lines:
        indent = len(list(takewhile(is_tab, line)))
        stack[indent:] = [line.replace(">","")]
        if stack[-1] == query:
        	count = count +1
        	out.write("\n")
       		for item in stack:
        		out.write("%s > " % item)
    out.write("\nQuantidade: " + str(count))



artefatos = open("changedMethodsFormated.txt", "r").read().split('\n')
callEntries = open("callEntries.txt", "r").read().split('\n')
coveredEntries = open("coveredPaths.txt", "r").read().split('\n')

for line in artefatos:
	out.write("Artefato modificado: " + line)
	out.write("\n - Caminhos possiveis - ")
	build_tree(callEntries, line)

	out.write("\n - Caminhos cobertos - ")
	build_tree(coveredEntries, line)
	out.write("\n\n")

