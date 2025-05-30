.. include:: links.rst

Development Guidelines
======================

The purpose of this documentation is to extend the CDM Development Guidelines by providing a structured approach to creating release notes for DRR that are clear, concise, and informative. This guideline covers the recommended content and style for DRR release notes, which should reflect accurately the changes made in the release. Furthermore, a template, serving as an example, can be downloaded here: :download:`DRR Release Notes Template </_downloads/source/contributing/DRR-release-notes-template.md>`

For further information related to the CDM, please, do check the corresponding CDM Development Guidelines in https://cdm.finos.org/docs/contribution#governance.

.. _content-of-release-notes:

Content of Release Notes
-------------------------

Release notes are text describing the content of any new DRR release and are a critical component of the distribution of that release. Release notes are edited in the *Mark-Down (MD)* syntax, which is then rendered into Html in the various channels where the release is published.

1. Release notes should begin with a high-level headline of the part of the model being changed, followed by "–" and a short headline description

   #. For example: "# DRR EMIR Refit - Clearing Threshold "

2. They should provide enough detail for a reviewer or other interested parties to be able to find and evaluate the change. For a data model change, for example, the data type and attributes should be named and the before/after states of the model explained, along with a justification in which the issue is summarised.
3. If the release notes describe reporting rules there should be a explicit information about the changes performed.
4.	If the release notes describe mapping rules, there should be explicit information about the examples affected and the change in resulting values for those examples.
5. If the release is documentation, it should specify exactly where the document was changed and why.
6. Special formatting rules related to use of the MD mark-up language:

   #. Headline should begin with a ``#``, as in the above example, so that it appears correctly formatted in Html
   #. ``*`` before and after text (no space) for bold
   #. ``_`` before and after text (no space) for italics
   #. ``–`` (plus a space) for bullets
   #. Backticks ````` before and after model components, e.g. data types, attributes, enums, function names, etc. for special code-style formatting

Example release notes formatted in MD:

.. code-block:: MD

  # *DRR EMIR Refit - Rule model updates*

  _Background_

  The existing implementation for the EMIR Refit rules that are fully or partially aligned with the CDE and CFTC jurisdictions has been reviewed and updated to match the latest CFTC version using common functions.

  _What is being released?_

  This second release fixes various issues identified in the EMIR Refit rules listed below:

  1. EMIR 2.1 - `UTI`:
    -    Replaced the filter based on the deprecated FpML's coding scheme unique-transaction-identifier by the CDM `identifierType`.
    -    Removed the filter to the last UTI version.

  2. EMIR 1.2 - `Report Submitting Entity ID`:
    - Changed the `SupervisoryBodyEnum` value to `ESMA`.

  _Review directions_

  In Rosetta, select the Textual View and search for the released rules.

  In Rosetta, open the reports tab, select the report `ESMA / EMIR Refit` and the dataset `Credit` and review the expectations for the field `2.1 - UTI` in the sample Credit-Swaption-Single-Name-ex01. 

  In Rosetta, open the reports tab, select the report `ESMA / EMIR Refit` and the dataset `Rates` and review the expectations for the field `1.2 - Report Submitting Entity ID` in the sample IR-Option-Swaption-ex01-Bermuda. 
  
 
The MD editing syntax in which release notes are written is a standard web mark-up language, for which a reference guide can be found at: https://www.markdownguide.org/cheat-sheet/

.. note:: The MD syntax provides similar features to the RST syntax (used to edit the user documentation), but the special formatting characters are slightly different between the two. While RST allows richer features that are useful for a full documentation website, MD is preferred for release notes because Slack supports (a subset of) the MD language and can therefore serve as a release publication channel.

Style
^^^^^

Content style
"""""""""""""

1. Content should be correct with regard to grammar, punctuation, and spelling (in British English), including but not limited to the following rules:

   #. Grammatical agreement, e.g. data types need, not data types needs
   #. Punctuation:

      #. etc. requires a period.
      #. Complete sentences should end with a period or colon (there should be no need for a question mark or exclamation point in these artefacts).
      #. Incomplete sentences cannot end with a punctuation.  For example, “Through the ``legalAgreement`` attribute the CDM provides support for implementors to:" is an incomplete sentence and cannot end in a punctuation. This can be fixed by adding a few words, e .g. “Through the ``legalAgreement`` attribute the CDM provides support for implementors to do the following:"
      #. Always use the Oxford Comma (aka the Serial Comma) for clarity when listing more than two items in a sentence, e.g. “data types, attributes, and enumerated values."  In extreme cases, failure to use this comma could be costly.

2. Other grammatical rules

   #. Agreement of numbers:  For example, if one sentence reads “the following initiatives…" , then it should be followed by more than one.
   #. Sentences should not end with a preposition

      #. Non-compliant example: “..to represent the party that the election terms are being defined for."
      #. Compliant: “...to represent the party or parties for which the election terms are being defined."

3. When a name or phrase is defined - continue to use it unless an alias has been defined. For example, one section reviewed had an expression "agreement specification details" but then switched to using "agreement content" without explanation. There is sufficient terminology to absorb, as such there is no need for synonyms or aliases, unless there are commonly used terms, in which case, they should be defined and one term should be used consistently.
4. User Documentation and descriptions should always be in the third person, for example: "the CDM model provides the following...". Never use the first person (including the use of "we").
5. In the user documentation, when there is a need for a long list, use bullets (``*`` or ``-`` followed by space, then text) as opposed to long sentences.
6. To the extent possible, use simple direct sentence structures, e.g. replace "An example of such" with "For example", or replace "Proposals for amendment to the CDM can be created upon the initiative of members of a Committee or by any users of CDM within the community who are not a current Committee member." with "Committee members or any user of CDM within the community can propose amendments to the CDM."
7. Exclude the usage of "mean to", “intends to", or “looks to".

   #. For example, "the model looks to use strong data type attributes such as numbers, boolean or enumerations whenever possible."
   #. Either the object works as designed or it does not. This expression might be used in a bug report when describing a function not working as intended but not to describe a production data model.

8.	Explain the CDM objects in an honest and transparent manner, but without criticism of the model. Sentences such as: "...which firms may deem inappropriate and may replace by..." or "the model is incomplete with regards to..." are unnecessary in a documentation. Rather, issues which may be identified in the CDM should be raised and addressed via the CDM governance structure.

Special format for CDM objects
""""""""""""""""""""""""""""""
1. Reporting rules and validation rules display rules:

    #. Reporting and validation rules names assigned in the model should be identified in the editor with code quotes, where the text between the quotes will appear in a special block format as illustrated here: ``NameOfTheFloatingRateOfLeg1`` for reporting rules, or ``EMIR_VR_2041_01`` for validation rules.


2. Data types and attributes display rules:

   #. Data types and attributes should be identified in the editor with code quotes, where the text between the quotes will appear in a special block format as illustrated here: ``LegalAgreementBase``.
   #. If the same word or phrase is used in a business context, as part of an explanation, then the words should be spaced and titled normally and the special format is not required: e.g. “Tradable products are represented by...".

3. Code snippets should be preceded by the string: ``.. code-block:: Language`` (where the Language could be any of Haskell, Java, JSON, etc.), followed by a line spacing before the snippet itself. The entire snippet should be indented with one space, to be identified as part of the code block and formatted appropriately. Indentation can be produced inside the snippet itself using further double space. Meta-data such as data type descriptions or synonyms that appear in the CDM should be excluded from the code snippet, unless the purpose of the snippet is to illustrate those.

Example of how a code snippet should be edited in the documentation:

.. code-block:: MD

 .. code-block:: Haskell

  type Party:
    [metadata key]
    partyId PartyIdentifier (1..*)
    name string (0..1)
      [metadata scheme]
    businessUnit BusinessUnit (0..*)
    person NaturalPerson (0..*)
    personRole NaturalPersonRole (0..*)
    account Account (0..1)
    contactInformation ContactInformation (0..1)

And the result will be rendered as:

.. code-block:: Haskell

 type Party:
   [metadata key]
   partyId PartyIdentifier (1..*)
   name string (0..1)
     [metadata scheme]
   businessUnit BusinessUnit (0..*)
   person NaturalPerson (0..*)
   personRole NaturalPersonRole (0..*)
   account Account (0..1)
   contactInformation ContactInformation (0..1)

.. note:: Code snippets that appear in the user documentation are being compared against actual CDM components during the CDM build process, and any mismatch will trigger an error in the build. This mechanism ensures that the user documentation is kept in sync with the model in production prior to any release.

Fonts, Text Styles, and Spaces
""""""""""""""""""""""""""""""

#. Bold should be used sparingly:

   #. Only in the beginning of a section when there is a salient point to emphasize, like a tag line - the bold line should be syntactically complete and correct.
   #. In the editor, bold is specified with double asterisks before and after the word or phrase.

#. Italics

   #. Italics should be used when defining an unusual term for the first time rather than using quotes, for example to identify something CDM specific, such as the concept of Primitive Events.
   #. In the editor, italics is specified with a single asterisk ``*`` before and after the word or phrase.

#. Single space should be used in-between sentences, not double space.

Style references for additional guidance
""""""""""""""""""""""""""""""""""""""""

#. `New Hart's Rules`_: An updated version of this erstwhile comprehensive style guide for writers and editors using British English, published by the Oxford University Press. Invaluable as an official reference on proofreading and copy-editing.  Subjects include spelling, hyphenation, punctuation, capitalisation, languages, law, science, lists, and tables. An earlier version coined the phrase Oxford Comma in July 1905.
#. `Eats, Shoots & Leaves: The Zero Tolerance Approach to Punctuation <https://www.lynnetruss.com/books/eats-shoots-leaves/>`_:  A light-hearted book with a serious purpose regarding common problems and correctness for using punctuation in the English language.