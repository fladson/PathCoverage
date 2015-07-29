from itertools import takewhile
import sys
import inspect

is_tab = '>'.__eq__

def build_tree(lines):
    lines = iter(lines)
    stack = []
    count = 0
    for line in lines:
        indent = len(list(takewhile(is_tab, line)))
        stack[indent:] = [line.replace(">","")]
        if len(stack) == 1:
            count = count + 1
    print count - 1 # Esta contando a ultima linha em branco



callEntries = open("callEntries.txt", "r").read().split('\n')
build_tree(callEntries)
