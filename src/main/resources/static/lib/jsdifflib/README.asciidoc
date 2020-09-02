== jsdifflib - A Javascript visual diff tool & library

* <<intro,Introduction>>
* <<overview,Overview>>
* <<python-interop,Python Interoperability>>
* <<demo,Demo & Examples>>
** <<diff-js,Diffing using Javascript>>
** <<diff-python,Diffing using Python>>
* <<status,Future Directions / Status>>
* <<license,License>>
* <<downloads,Downloads>>
* <<history,Release History>>

[[intro]]
== Introduction

http://cemerick.com[I] needed a good in-browser visual diff tool, and couldn't find anything suitable, so I built *jsdifflib* in Feb 2007 and open-sourced it soon thereafter.  It's apparently been used a fair bit since then.  Maybe you'll find it useful.

===== If you *do* find jsdifflib useful, _please support my open source work via a bitcoin donation/tip_ to 19qCqZxAdRF4eZfyZD2GQnAWk2Mz7DZZVf.  Thanks!

[[overview]]
== Overview

jsdifflib is a Javascript library that provides:

. a partial reimplementation of Python's difflib module (specifically, the SequenceMatcher class)
. a visual diff view generator, that offers side-by-side as well as inline formatting of file data

Yes, I ripped off the formatting of the diff view from the Trac project. It's a near-ideal presentation of diff data as far as I'm concerned. If you don't agree, you can hack the CSS to your heart's content.

jsdifflib does not require jQuery or any other Javascript library.

[[python-interop]]
== Python Interoperability

The main reason why I reimplemented Python's difflib module in Javascript to serve as the algorithmic basis for jsdifflib was that I didn't want to mess with the actual diff algorithm -- I wanted to concentrate on getting the in-browser view right. However, because jsdifflib's API matches Python's difflib's SequenceMatcher class in its entirety, it's trivial to do the actual diffing on the server-side, using Python, and pipe the results of that diff calculation to your in-browser diff view. So, you have the choice of doing everything in Javascript on the browser, or falling back to server-side diff processing if you are diffing really large files.

Most of the time, we do the latter, simply because while jsdifflib is pretty fast all by itself, and is totally usable for diffing "normal" files (i.e. fewer than 100K lines or so), we regularly need to diff files that are 1 or 2 orders of magnitude larger than that. For that, server-side diffing is a necessity.

[[demo]]
== Demo & Examples

You can give jsdifflib a try without downloading anything. Just click the link below, put some content to be diffed in the two textboxes, and diff away.

http://cemerick.github.com/jsdifflib/demo.html[*Try jsdifflib*]

That page also contains all of the examples you'll need to use jsdifflib yourself, but let's look at them here, anyway.

[[diff-js]]
=== Diffing using Javascript

Here's the function from the demo HTML file linked to above that diffs the two pieces of text entered into the textboxes on the page:

----
function diffUsingJS() {
    // get the baseText and newText values from the two textboxes, and split them into lines 
    var base = difflib.stringAsLines($("baseText").value);
    var newtxt = difflib.stringAsLines($("newText").value);

    // create a SequenceMatcher instance that diffs the two sets of lines
    var sm = new difflib.SequenceMatcher(base, newtxt);

    // get the opcodes from the SequenceMatcher instance 
    // opcodes is a list of 3-tuples describing what changes should be made to the base text 
    // in order to yield the new text
    var opcodes = sm.get_opcodes();
    var diffoutputdiv = $("diffoutput");
    while (diffoutputdiv.firstChild) diffoutputdiv.removeChild(diffoutputdiv.firstChild);
    var contextSize = $("contextSize").value;
    contextSize = contextSize ? contextSize : null;

    // build the diff view and add it to the current DOM
    diffoutputdiv.appendChild(diffview.buildView({
        baseTextLines: base,
        newTextLines: newtxt,
        opcodes: opcodes,
        // set the display titles for each resource 
        baseTextName: "Base Text",
        newTextName: "New Text",
        contextSize: contextSize,
        viewType: $("inline").checked ? 1 : 0
    }));

    // scroll down to the diff view window.
    location = url + "#diff";
}
----

There's not a whole lot to say about this function. The most notable aspect of it is that the `diffview.buildView()` function takes an object/map with specific attributes, rather than a list of arguments. Those attributes are mostly self-explanatory, but are nonetheless described in detail in code documentation in diffview.js.

[[diff-python]]
=== Diffing using Python

This isn't enabled in the demo link above, but I've included it to exemplify how one might use the opcode output from a web-based Python backend to drive jsdifflib's diff view.

----
function diffUsingPython() {
    dojo.io.bind({
        url: "/diff/postYieldDiffData",
        method: "POST",
        content: {
            baseText: $("baseText").value,
            newText: $("newText").value,
            ignoreWhitespace: "Y"
        },
        load: function (type, data, evt) {
            try {
                data = eval('(' + data + ')');
                while (diffoutputdiv.firstChild) diffoutputdiv.removeChild(diffoutputdiv.firstChild);
                $("output").appendChild(diffview.buildView({
                    baseTextLines: data.baseTextLines,
                    newTextLines: data.newTextLines,
                    opcodes: data.opcodes,
                    baseTextName: data.baseTextName,
                    newTextName: data.newTextName,
                    contextSize: contextSize
                }));
            } catch (ex) {
                alert("An error occurred updating the diff view:\n" + ex.toString());
            }
        },
        error: function (type, evt) {
            alert('Error occurred getting diff data. Check the server logs.');
        },
        type: 'text/javascript'
    });
}
----

[WARNING]
====
This dojo code was written in 2007, and I haven't _looked_ at dojo for years now.  In any case, you should be able to grok what's going on.
====

As you can see, I'm partial to using dojo for ajaxy stuff. All that is happening here is the base and new text is being POSTed to a Python server-side process (we like pylons, but you could just as easily use a simple Python script as a cgi). That process then needs to diff the provided text using an instance of Python's difflib.SequenceMatcher class, and return the opcodes from that SequenceMatcher instance to the browser (in this case, using JSON serialization). In the interest of completeness, here's the controller action from our pylons application that does this (don't try to match up the parameters shown below with the POST parameters shown in the Javascript function above; the latter is only here as an example):

----
@jsonify
def diff (self, baseText, newText, baseTextName="Base Text", newTextName="New Text"):
    opcodes = SequenceMatcher(isjunk, baseText, newText).get_opcodes()
    return dict(baseTextLines=baseText, newTextLines=newText, opcodes=opcodes,
                baseTextName=baseTextName, newTextName=newTextName)
----

[[status]]
== Future Directions

The top priorities would be to implement the ignoring of empty lines, and the indication of diffs at the character level with sub-highlighting (similar to what Trac's diff view does).

I'd also like to see the `difflib.SequenceMatcher` reimplementation gain some more speed -- it's virtually a line-by-line translation from the Python implementation, so there's plenty that could be done to make it more performant in Javascript. However, that would mean making the reimplementation diverge even more from the "reference" Python implementation. Given that I don't really want to worry about the algorithm, that's not appealing. I'd much rather use a server-side process when the in-browser diffing is a little too pokey.

Other than that, I'm open to suggestions.

[NOTE]
====
I'm no longer actively developing jsdifflib.  It's been sequestered (mostly out of simple neglect) to my company's servers for too long; now that it's on github, I'm hoping that many of the people that find it useful will submit pull requests to improve the library.  I will do what I can to curate that process.
====

[[license]]
== License

jsdifflib carries a BSD license. As such, it may be used in other products or services with appropriate attribution (including commercial offerings). The license is prepended to each of jsdifflib's files.

[[downloads]]
== Downloads

jsdifflib consists of three files -- two Javascript files, and one CSS file. Why two Javascript files? Because I wanted to keep the reimplementation of the python difflib.SequenceMatcher class separate from the actual visual diff view generator. Feel free to combine and/or optimize them in your deployment environment.

You can download the files separately by navigating the project on github, you can clone the repo, or you can download a zipped distribution via the "Downloads" button at the top of this project page.

[[history]]
== Release History

* 1.1.0 (May 18, 2011): Move project to github; no changes in functionality
* 1.0.0 (February 22, 2007): Initial release
