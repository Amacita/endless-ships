# Endless Sky reference site generator

This is a tool for generating a website with reference information about the [Endless Sky](https://endless-sky.github.io/) game.

The generator works by parsing the game's sources (specifically `data/*.txt` files) to get all game data so the information always stays up-to-date.

## Installation

First you need to clone the project and pull the game which is linked as a git submodule:

``` sh
$ git clone git@github.com:amacita/endless-ships.git
$ cd endless-ships
$ git submodule update --init
```

The backend uses [Boot](http://boot-clj.com/), so you need to [install](https://github.com/boot-clj/boot#install) it (`brew install boot-clj` on OS X). It installs all the other Clojure dependencies upon first launch.

The frontend is compiled using shadow-cljs, which is an npm module. To install it, you need to [install yarn](https://yarnpkg.com/en/docs/install) (`brew install yarn` on OS X).

## Generating the site

Run `boot generate-data` from the root directory of the application. If there are any parse errors, it will tell you and abort. If the parsing is successful, it will save the generated data as `public/data.edn`.

## Viewing the site locally

The easiest way to view the site is to run `shadow-cljs watch main` and point your browser to [http://localhost:8080](http://localhost:8080).

## Troubleshooting

Most problems can be resolved by fixing the underlying error in the plugin data, or by configuring `src/plugins.clj` to ignore specific files known to have issues.

### Front-end problems

- Submunitions that don't have a `lifetime` defined.
- Ships that don't have a `category` defined will cause the entire ships page to break.
- Ship variants that aren't proper variants, e.g. `ship "Fury (Missiles)"` instead of `ship "Fury" "Fury (Missiles)"`, will result in broken links to those variants.

### Back-end problems

These problems occur when the parser sees something it doesn't like. You will get a detailed parse error when it breaks. These are some examples of data file problems that have caused parse errors:

- Missing a space between an attribute and its value, e.g. `"shields"500`
- Trying to place a comment with `//` instead of `#`
- System names or other identifiers containing `#`
- Extra indentation levels
- Separating things with tabs instead of spaces, e.g. `mass <tab> 500`
- Indenting with spaces instead of tabs
- Mission files missing an end \` in a conversation node

## Plugin support

You can add support for additional plugins by editing `src/clj/endless_ships/plugins.clj`. The `plugins` data structure contains entries for vanilla Endless Sky and each of the supported plugins. You will also have to add the plugin as a git submodule (`git submodule add <github repository> resources/<plugin>`). Once you've done that, `boot generate-data` should automatically pick it up and update `public/data.edn` if there are no parse errors.

The backend assumes that the plugin is organized like `data/<race>/*.txt`. If not, you will have to add race override entries for it in the `plugins` data structure.

Finally, you will want to add css labels in `public/app.css` for each new government.

## Working with the code

You can skip this section if you don't intend to modify the source code, or if you already understand how to work with Clojure.

This fork was developed on Ubuntu (WSL), using Neovim and the Conjure plugin. The source code is divided in two parts, Clojure for the backend (`src/clj`) and ClojureScript for the frontend (`src/cljs`).

To work with the backend, run `boot repl` and open the `clj` files in Neovim. Conjure should detect the running repl and automatically connect.

To work with the frontend, run `shadow-cljs --debug watch main` and open the `cljs` files in Neovim. Point your web browser to [http://localhost:8080](http://localhost:8080) and open the development console (Ctrl-shift-i in Firefox). Then in Neovim, run `:ConjureConnect 3333` and `:ConjureShadowSelect main`.

Regular Vim and the VimFireplace plugin are not recommended.
