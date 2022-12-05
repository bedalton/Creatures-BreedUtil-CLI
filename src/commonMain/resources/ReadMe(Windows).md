# Creature's Breed Util

Command line tools for parsing, compiling or converting Creatures sprite files

## Basic Information
This is a command line program, and must be run from the CMD program on Windows or terminal on unix.
Be aware of your current working directory as all non-absolute paths will be relative to it.


### Install
EXE version: (Windows only)
There is a windows release that does not require node
Download the `breed-util.exe` file from releases. Extract the contents and copy to the folder of your choosing. 
Optionally you can add this directory to your PATH variable, so it can be referenced from anywhere

NodeJS Version: (all OS's)
There is a NodeJS version that will work on non-windows platforms. To use NodeJS must be installed.

To download node, visit [node's downloads page](https://nodejs.org/en/download/)

```npm install -g @bedalton/breed-util```


Executable can be referenced by absolute path or relative path if not added to the system PATH variable.

## Guided Breed Conversion
When converting a breed, one can launch the exe without arguments and follow the command prompts
When asking for a folder or file, one can drag the file or folder from Explorer into the command prompt window

**Note** Though this program converts the images and base ATTs, additional editing to ATTs may be necessary
to allow for seamless breeding between your converted breed and existing breeds in your target game.

---

The `breed-util` utility allows for the following sub-commands:

### Base Commands
**[ask](#Guided Breed Conversion)**   
Use question and answer based breed conversion 

**[convert-breed](#convert-breed)**  
Convert a breed's images (and optionally its ATTs) from one game to another

**[alter-genome](#alter-genome)**  
Alter the appearance genes of Creatures genome

```
Usage: 
    breed-util ask
    breed-util convert-breed [options_list] inputFiles...
    breed-util alter-genome [options_list]
    
Subcommands: 
    ask - Guided conversion of a breed using quesions/prompts
    convert-breed - Convert breed files between game formats
    alter-genome - Alter appearance genes in a Creatures genome

Options: 
    --help -> 
        Usage info for base command `breed-util --help`, 
        or child command. i.e. `breed-util convert-breed --help`
```

### Input Files
The **inputFiles** argument contains one or any mix of the following:
- Individual file names
- Directories
- Glob patterns. i.e. `Frost-Grendel\*.spr`

**Files are:**
- used in the order they are defined
- If input is a folder or glob, files are sorted in the following order
  - Folders
  - File name without leading or trailing numbers
  - Leading numbers
  - Trailing numbers

**Note** The numbers in the file name are **sorted numerically not by text**.    
So:
```
file-033
file-3
file-20
```
Would be sorted as
```
file-3
file-20
file-033
```
And:
```
01-file-3
20-aFile-033
02-aFile-20
```
Would be sorted as
```
02-aFile-20
20-aFile-033
01-file-3
```
Because `aFile` comes before `file` when sorted by text, and is evaluated before leading number

---

## ASK

Walks through converting a breed by using command line prompts.  
- Each question will ask for input.   
- After typing, pasting, or dragging in a file or folder, hit enter to submit your answer
and move on
  - Some questions have a default answer maked by the text (default). Simply hit enter to accept

---

## Convert-Breed

Converts a breed's sprites from one game to another and optionally its ATTs as well

For C1 <-> C2 or C2e if the breed has tails, the parts are
automatically reversed as is needed for C1 tails to the other games and vice-versa
**Progressive Arms**
C1/C2 breeds only have one front facing pose, so when converted, norns will appear to stare straight at the camera while not moving.

Progressive arms adds front facing arm poses for C1/C2 conversions to C3DS. It does so using the side views of the arms.

![Two images of a male C1 horse norn. One with his arm across his stomach, and the another with an arm raised up as if saying hi](images/progressive-arms.png)

**Missing Tails:** Empty tail sprite and optionally ATTs will be generated automatically
if the C1 breed does not have one defined

**Note:** This utility does **not** support **Creatures Village/Adventure** in any direction

**Alter Genome**
Optionally an existing genome for the target game can have its appearance genes altered
Genome alteration also alters the sleep pose between C1e and C2e conversions to prevent 
creatures from sleeping/dying standing up  
*\*note:* This program does not convert a genome, it simply alters an existing genome for the target game  
i.e. If converting to C3, you must supply an existing C3 genome. *No conversion is done*

```
Usage: breed-util convert-breed target-game [options_list] inputFiles...
Arguments: 
    type -> Target game for breed files { Value should be one of [C1, C2, CV, C3, DS, SM] }
    images -> Image files or folders { String }
Options: 
    --from, -Input sprite's game variant (C1|C2|CV|C3|DS) { Value should be one of [C1, C2, CV, C3, DS, SM] }
    --encoding, -c -> Sprite color encoding { Value should be one of [555, 565] }
    --genus, -g -> The output genus: [n]orn, [g]rendel, [e]ttin, [s]hee, geat { Value should be one of [n, norn, g, grendel, e, ettin, s, shee, geat] }
    --breed, -b -> The output breed slot for these body parts { String }
    --input-genus -> The genus to filter input files by { String }
    --input-breed -> The breed to filter input files by { String }
    --force, -f [false] -> Force overwrite of existing files 
    --progressive [false] -> Use non-linear mapping of C1e to C2e parts to fake front facing tilt 
    --keep-ages [false] -> Do not shift ages to match target game 
    --skip-existing, -x [false] -> Skip existing files 
    --no-tail [false] -> Do not create tail files (even if none are present) 
    --ignore-errors, -e [false] -> Ignore all compilation errors. Other errors will still cancel compile 
    --quiet, -q [false] -> Silence non-essential output 
    --progress, -p [false] -> Output file conversion progress 
    --att-dir, -a -> The location of atts to convert if desired { String }
    --output, -o -> Output folder for the converted breed files { String }
    --samesize, -z [false] -> Make all frames in a body part the same size 
    --input-genome -> Input genome file to alter appearance genes for { String }
    --output-genome -> Altered genome output file path { String }
    --help, -h -> Usage info 
Help:
    --help, -h -> Usage info 
```

### Example usage

**# Example 1: Convert a breed "simple"**
The following would convert a breed's sprites to C3, keeping the genus, age and breed slot the same
```console
breed-util convert-breed C3 Images::PATHSEP::*0*8.spr Images::PATHSEP::*4*8.spr
```

**# Example 1: Change slot**
The following code would produce a C3 compatible breed sprites in `ettin slot z` from a `norn slot 8`
```console
breed-util convert-breed C3 --genus ettin --breed z Images::PATHSEP::*0*8.spr Images::PATHSEP::*4*8.spr
```

**# Example 3: Convert ATTs**
To convert ATTs in addition to sprites, use the `--att-dir, -a` option followed by the ATT directory
So to convert a norn slot 8 breed from C1 to C3 ettin z you would use:
```console
breed-util convert-breed C3 --genus ettin --breed z --att-dir "Body Data" Images::PATHSEP::*0*8.spr Images::PATHSEP::*4*8.spr
```

**NOTE** Despite the ATTs being converted automatically.
Care should be taken to edit head and body sprites converted from C1/C2 as the
tail, hair and ear positions will be set to `0,0`. This will be very noticeable when bred with other breeds

**# Example 4: Without tail**
The prior commands will produce tail sprites (and atts if --att-dir is set)
by default if one does not exist for the breed.  
To prevent this, use the `--no-tail` flag.  
The following would produce norn sprites and ATTs for parts `a` to `l`, but not `m` or `n`.
```console
breed-util convert-breed C3 --genus ettin --breed z --no-tail --att-dir "Body Data" Images::PATHSEP::*0*8.spr Images::PATHSEP::*4*8.spr
```

**# Example 5: Remap a breed**  
You can technically remap a breed by passing in the same game as the one it is for, but with different slot information  
The following would create a copy of norn slot `a` into ettin slot `z`;
** Be sure to include the `--att-dir` flag or the body data will not be remapped
```console
breed-util convert-breed C3 --genus ettin --breed z --att-dir "Body Data" Images::PATHSEP::*0*a.c16 Images::PATHSEP::*4*a.c16
```


## Alter-Genome

Alters a genome's appearance genes. This allows you to set different genus/breeds per part

Appearance genes follow the format: `{genus}:{breed}`.   
Genus can be norn or n, grendel or g, ettin or e, geat or shee or s.  
**NOTE:** The shorthand for GEAT is '**S**' not G (G is Grendel)

```SHELL
Arguments: 
    input-genome -> The genome to alter { String }
Options: 
    --output-genome -> Altered genome output file path { String }
    --genus, -g -> The output genus: [n]orn, [g]rendel, [e]ttin, [s]hee, geat { Value should be one of [n, norn, g, grendel, e, ettin, s, shee, geat] }
    --breed, -b -> The default breed slot for all body parts { String }
    --head -> Breed for head Expected pattern  "norn:a" or "n:z"; 'g:' is grendel. use 's:' for geat
    --body -> Breed for body Expected pattern  "norn:a" or "n:z"
    --legs -> Breed for legs Expected pattern  "norn:a" or "n:z"
    --arms -> Breed for arms Expected pattern  "norn:a" or "n:z"
    --tail -> Breed for tail Expected pattern  "norn:a" or "n:z"
    --hair -> Breed for hair Expected pattern  "norn:a" or "n:z"
    --alter-sleep, -s [false] -> Alter the sleep pose to handle conversions between C1e and C2e
    --force, -f [false] -> Force overwrite of existing files 
    --skip-existing, -x [false] -> Skip existing files 
    --help, -h -> Usage info 

```