# Endless Sky reference site generator

This is a tool for generating a website with reference information about the [Endless Sky](https://endless-sky.github.io/) game.

The generator works by parsing the game's sources (specifically `data/*.txt` files) to get all game data so the information always stays up-to-date.

## Installation

First you need to clone the project and pull the game which is linked as a git submodule:

``` sh
$ git clone git@github.com:7even/endless-ships.git
$ cd endless-ships
$ git submodule update --init
```

The generator uses [Boot](http://boot-clj.com/) so you need to [install](https://github.com/boot-clj/boot#install) it (`brew install boot-clj` on OS X). It installs all the other Clojure dependencies upon first launch.

The client-side part of the site is being compiled using [yarn](https://yarnpkg.com) so you need to [install](https://yarnpkg.com/en/docs/install) it as well (`brew install yarn` on OS X).

## Generating the site

Just run the `boot build` command from the root directory of the application. It will put the generated files under the `build/` directory, ready to be deployed to the server.

## Viewing the site locally

If you don't want to deploy the site to the server you can view it locally - there's [a bunch of ways](https://gist.github.com/willurd/5720255) to start a simple HTTP server for serving static files. For example here's how one would do that with Ruby:

``` sh
$ cd build # the generated site will be available at build/
$ ruby -rwebrick -e'WEBrick::HTTPServer.new(:Port => 8000, :DocumentRoot => Dir.pwd).start'
```

Then point your browser to [http://localhost:8000](http://localhost:8000).

## Troubleshooting

Here are some things known to break the program.

- Submunitions that don't have a `lifetime` defined.
- Ships that don't have a `category` defined.
- Invalid syntax
   - Missing a space between an attribute and its value, e.g. `"shields"500`
   - Trying to place a comment with `//` instead of `#`
   - System names or other identifiers containing `#`
   - Extra indentation levels
   - Separating things with tabs instead of spaces, e.g. `mass <tab> 500`
   - Indenting with spaces instead of tabs
   - Mission files missing an end \` in a conversation node

If you want the parser to ignore certain non-conformant files, just add them to the ignore list in `src/plugins.clj`.
