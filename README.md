# net.FunkyJava.Gametheory

This Java library has or had several goals :
- implement generic CFRM (Counter Factual Regret Minimization) algorithms
- provide tools to build games on which those algorithms can run
- run competitions between strategies computed for the same games
- try to provide a great object model and a full documentation to make it as easy to use as I can

The main practical goal I had when starting the implementation was to run studies on the particular point of stacks evaluations in NLHE (No-Limit Hold'Em Poker).

In particular, I wasn't satisfied in the common consideration that the chances of winning a HU (Heads-Up) SNG (seat-and-go) was proportional to the chips sizes of the stacks. When you see that in some situations, the SB (small-blind) player has an EV of say +0.5 blinds, I can't ignore it and guess I shouldn't take it into account.

So I implemented all what I needed to perform basic tests on a simple push/fold heads-up game by computing Nash equilibrium strategies with usual stacks valuation and a modified one based on sliding values modified by random iteration on many subgames (hands) of the SNG tournament (I called such a global game a Cyclic Steps Game).

I'll tell more about this work later (or you can just look at the code) but first, let's describe the state of this library and all of its components.

## Technical prerequisites

You MUST use Lombok ( https://projectlombok.org ) to properly open the project in your IDE (I use Eclipse but it's easy in other IDEs too).

And you MUST learn how to use Maven if you don't already know :)

## Code and model state and inventory

Most of the code is fully commented (JavaDoc) and well formatted.

Some classes are implementations found over the web, particularly on http://poker-ai.org (special thanks to this forum that gave me pretty all the material I needed to learn!). 

Many parts will be refactored or replaced as I'm no more satisfied of their implementation or model.

Everywhere the performance doesn't matter I put checks (mainly using Guava) and logs (SLF4J) 

Most of the code has unit tests, but it's there are holes in lately implemented parts or global tests that maybe shouldn't be considered as unit tests.

Most of the code logic can be read in the Javadoc so I won't repeat myself copying it,now let's just look at all Maven artifacts to describe the architecture of this library (I ommit the net.funkyjava.gametheory prefix).

### commonmodel

This artifact intends to describe an extensive game from the outside, the playing point of view.
There is only a hierarchy of simple interfaces for observing a game, walking its tree or implement "deciders" (or players if you prefer) and one class for describing a game node.

### play

This one runs confrontation between deciders, usefull to compare who performs the best for two strategies on the same game. Also a class to run Cyclic Steps Games versus usual ones (see kuhn poker and push/fold examples).

### CSCFRM (Chance-Sampling CFRM)

I based many of what I developed on this particular algorithm that has the advantage of requiring less work to build suitable games. But I'm fully aware it doesn't fit for many other games, it is slower in many cases, and my implementation is slow.
I know I could optimize many things in it because I know much more now about execution optimization than when I started this project.

But I don't think I'll optimize it for now because I would like to build a better model for my generic CFRM engine and I'll discuss it later in this presentation.

I happily tried to have a code as modular as possible so this doesn't affect all the artifacts and many of them can be used independently.

#### cscfrm.model

This artifacts groups together game interfaces to make them eligible to run CS-CFRM, also an abstraction of the nodes implementation and a special interface for Cyclic Steps Games.

#### cscfrm.core

Here you'll find 
- the CSCFRM algorithm implementation
- some beans for its config, state...
- interfaces for loading and saving the nodes strategies and the CSCFRM execution state
- Abstraction of its utility reader (because you may want to tweak it as I wanted for my Cyclic Steps Games)

#### cscfrm.exe

Everything you need to execute CSCFRM (monothread, multithread and Cyclic Steps Games), and a convenient Workstation to manage your CSCFRM execution.

#### cscfrm.impl

Default implementations of many things :
- nodes
- CSCFRM execution loaders (be careful when using the FileChannelLoaderProvier and use it in a safe folder as it can delete recursively everything in it when you use its clear method, but it's never called automatically :P )
- a default Workstation using the default nodes

#### cscfrm.util

Useful tools to build and validate your game.

#### cscfrm.games (.kuhnpoker and .poker.nlhe.pushfold)

Example games eligible to CSCFRM and playing their strategies. All related execution classes, even battles between Cyclic Steps games and usual ones. Old and unoptimized implementations :P

### gameutil

Here are stand-alone tools that can be used for building games, for now only holds poker-related tools.

#### gameutil.cards

I made the choice to represent cards as integers. It means one card is one integer. It may be a bad choice as masks are often used by many cards tools (indexers or so), and it may change in the future.

This artifact contains cards formalism specifications, convenient deck class, drawing tools, indexing interfaces, look-up tables (LUT) and bucketing abstractions.

LUT abstraction was recent and I didn't have time to review it deeply, maybe many bad choices in there.

#### gameutil.cards.indexing.bucketing.kmeans

A multithread tweak of apache.math KMeans++ implementation. Not from me ( found here http://poker-ai.org/phpbb/viewtopic.php?f=24&t=2602 => https://github.com/flopnflush/kmeans ), never modified or bound to my internal interfaces (as far as I remember).

#### gameutil.poker.bets

This one is from me. Its goal was to implement NLHE betting rules safely (many checks everywhere) to build a reduced betting tree.
It's not optimized at all because it's not the point, the priority was to not make any mistake. Once you get your reduced tree, you should be able to walk it efficiently.

It's a recent code so it may contain some mistakes but it's pretty solid so I doubt it.

#### gameutil.poker.he.evaluators

Contains mainly one class : AllHoldemHSTables that can pre-compute HS, EHS and EHS2 for all streets, save it to a zip file or load it. Values are accessed by Waugh's indexers instances that can be created by instanciating this class.
To avoid the one hour computation, you can download the computed tables here : http://uptobox.com/69dp3m8xe1p1

#### gameutil.poker.he.handeval

Interfaces to evaluate HE hands (flop, turn and river).

#### gameutil.poker.he.handeval.twoplustwo

The famous Hold'Em hand evaluator from the 2+2 forum, interfaced to this library.
Original post : http://archives1.twoplustwo.com/showflat.php?Number=8513906
Maybe other implementations are more optimized, but this one works very well :)
This one maybe ? https://gist.github.com/EluctariLLC/832122

#### gameutil.poker.he.indexing.djhemlig

Djhemlig's LUTs for Hold'Em from http://www.poker-ai.org/archive/pokerai.org/pf3/viewtopice906.html?f=3&t=2777&hilit=lut
(I think)

#### gameutil.poker.he.indexing.waugh

I'm proud of this one because it was a personal coding challenge.

The algorithm was presented by Kevin Waugh in this paper from the AAAI 2013 Workshop : https://www.aaai.org/ocs/index.php/WS/AAAIW13/paper/download/7042/6491 

Poker-AI topic : http://poker-ai.org/phpbb/viewtopic.php?f=25&t=2660

Kevin Waugh's website : http://www.cs.cmu.edu/~kwaugh/

It's a perfect cards indexer taking colors permutations into account. As it's a bijection between canonical hands and indexes, you can retrieve a canonical hand from its index. Of course it's not the better LUT because it doesn't care of maximum hands for example that can be considered the same in poker. But you can easily build such a LUT on top of it.

My implementation was not inspired at all by Waugh's one (in C) because I found it too hard to read :P

It performs about twice slower than Djhemlig's LUT for flop (6M indexings/s vs 12M on my computer), but it isn't the same thing as it is isomorphic and very flexible : you can index any number of groups of any number of cards with it so it can be used for many games.

The only current limitation of my implementation is the use of integers instead of longs for the indexes that limitates it to the max Java integer value. So you can't for example index the Hold'Em river with perfect recall. 


### Conclusion

For me, this is a background project. I mainly work on it when I have time and no rush in my current job.

But I want to improve it, refactor it and optimize it :)

Mainly I would like to implement another game abstraction that describes the chances repartitions in a way that a generic CFRM algorithm could find everything it needs to run efficiently, not always calling game's methods, and so we would get :
- a game description fully separated of the CFRM implementation
- the possibility to write many implementations (Pure CFRM, AS CFRM, and why not MCTS...)
- performances improvements

I also would like to complete poker tools for clustering and many other missing things.

Don't hesitate to contact me if you're interested in contributing !

Ah, and this code is free blabla.

Thanks for reading at least the last line :P
