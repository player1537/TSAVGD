#!/bin/bash

sed -e 's/[{}]/\n&\n/g; s/;/;\n/g' | sed -e '/}/ { x; s/^  //; x; }; G; s/\(.*\)\n\(.*\)/\2\1/; /{/ { x; s/^/  /; x; }'
