# Aid Platform Beta

> @Rchards -> @johnthegeo @dfid_uk @kouphax @alisoncoote I have great hopes that it will help change the debate on our aid budget - people will see impact

## Contents

1. [Introduction](#-introduction)
	1. [Useful Links](#-useful-links)
2. [Project Management](#-project-management)
	1. [Sprint Structure](#-sprint-structure)
	2. [Definition of done](#definition-of-done)
	3. [Sprint Goals](#-sprint-goals)
	4. [Key Dates](#-trello-boards)
	5. [Expected Holidays](#-expected-holidays)
	6. [Trello Boards](#-trello-boards)
3. [Architecture](#-architecture)
4. [Development Guidelines](#-development-guidelines)
	1. [Make Commits Meaningful](#-make-commits-meaningful)
	2. [Dont Commit to Master](#-dont-commit-to-master)
	3. [Run all tests before pushing](#-run-all-tests-before-pushing)
5. [Setup Notes](#-setup-notes)
	1. [Environment Variables](#-necessary-environment-variables)
6. [Contributors](#-contributors)

## <a name="introduction"></a> Introduction

Beta version of the DFID Aid Information Platform

### <a name="useful-links"></a> Useful Links

- [IATI Standard](http://iatistandard.org) - provides detail on the IATI XML Standard and Validation rules and notes.
- [IATI Registry](http://iatiregistry.org) - provides searchable access ot all the published IATI files available.

## <a name="project-management"></a> Project Management

The project uses a minimum set of tools to allow us to manage the project.  If you need access to any of these talk to James, Alison or John

- Trello (Product Backlog and Planning) - [Aid Platform Beta Board](https://trello.com/dfid)
- HipChat (Team Communication) (__PENDING__)
- Github (Version Control, Issues, Pull Requests) - [Aid Platform Beta](https://github.com/DFID/aid-platform-beta)
- Planning Poker - For sprint planning - [planningpoker.com](http://kouphax.planningpoker.com/)

### <a name="sprint-structure"></a> Sprint Structure

- 2 day inception (31st Jan -> 1st Feb)
- 2 week sprints 
- First Sprint - (4th Feb -> 15th Feb)
- Second Sprint - (18th Feb -> 1st Mar)
- Third Sprint - (4th Mar - > 21st Mar) 
- Final Sprint - (22nd Mar -> 5th Apr)
- Prep for Beta - (8th Apr -> 26th Apr)

### <a name="definition-of-done"></a> Definition of Done

- Acceptance Criteria met and tested against the following browsers
    - 	Desktop	Internet Explorer 9
    - 	Desktop	Chrome 23
    -	Desktop	Internet Explorer 8
    - 	iOS Safari 6
    - 	Desktop Firefox 16
    - 	Desktop	Internet Explorer 7
    - 	Desktop	Safari 6
    - 	iOS Safari 5.1

- Code committed with acceptable explanation of purpose and changes (inc. Tests)
- Pull Request opened, reviewed and accepted by Team, merged by TDA
    - CI Server build and passes all tests
- Trello acceptance criteria all met for story specific stuff
- Accepted by PO/Relevant Stakeholder

### <a name="sprint-goals"></a> Sprint Goals

__Key Goals__

- Move away from Operational Plan
- Bring in Global and Regional Project Navigation
- Build out public API
- More...

### <a name="key-dates"></a> Key Dates

- Potential user testing after 2nd Sprint.
- Potential user testing after last Sprint.
- Potential Penetration Testing at start of last Sprint.

### <a name="expected-holidays"></a> Expected Holidays

- Clare Maclennan - 3 Weeks (25th Feb -> 15th March)
- James Hughes - 3 Days (18th March -> 20th March)
- John Adam - 1 week (18th March -> 22nd March)
- Kyle Davidson - 1 Day (15th February)
- Alison Coote - 2 Days (21st March - 22nd March)

### <a name="trello-boards"></a> Trello Boards

A number of trello boards have been created to facilitate project planning and management.  It is intended these will be refined over time.  In order to facilitate sprint planning and estimation it is possible to use the Chrome based [Scrum Plugin for Trello](https://chrome.google.com/webstore/detail/scrum-for-trello/jdbcdblgjdpmfninkoogcfpnkjmndgje?hl=en) though not essential.

__[Outstanding Questions](https://trello.com/board/outstanding-questions/5107b23cba70c4eb7b00a1d6)__

In order to ensure the need to "get our data right" doesn'tpollute the product backlog or sprints there is a separate board for any data related or project related questions.

__[Product Backlog](https://trello.com/board/product-backlog/5107b28fe4ed2cd07b00381a)__

The product backlog represents all the items to potentially include in the Beta release.  It uses the following lists,

- Ideas - Coarse grained items to bring into the beta.  Not necessarily fully understood or ready yet but roughly prioritized
- In Progress - Should be called something else - simply a scratchpad for working on fleshing out items form the __Ideas__ list into deliverable features.
- Ready Epics - These are epics that have been fleshed out and have features representing the epic in either Ready/Delivery/Live.  Each epic should have a list of realted features (use the `#CARD_NO` notation to allow easy linking to them)
- Ready - Features/Chores that are defined enough (Definition of Ready) that can be worked on by the Dev Team
- Sprint Candidates - Items in here will be used for sprint planning - should be __prioritized__ as best as possible.  Sprint Planning will be done through [PlanningPoker](http://kouphax.planningpoker.com/)
- Live - Items that have been successfully delivered during a sprint.

The __Sprint Candidates__ list will be used as a basis for sprint planning.  Items from this list can be moved into the current sprints initial list once estimated.

> There is currently an issue in trello that removes the labels from cards when moving cards between boards.  We can do this manually for now.

__[Sprint](https://trello.com/board/sprint-1/5107b4bee77a73bb56009a41)__

The sprint boards will be used by the team to ensure they are on track to deliver everything they brough into the sprint.

- Sprint Backlog - The features and chores that the team will attempt to deliver during this sprint
- In Progress - Items the team are currently working on (it is essential for viibility that we know who is working on feature)
- Delivered - Items that have been completed by the dev team but not yet fully reviewed by the Product Owner
- Accepted - Items that have been accepted by the Product Owner - they will stay here until the end of the sprint and moved during sprint review.  During sprint review these hould be moved to the __Live__ list in the Product Backlog to give us an overall view of the current state of the system
- Rejected - Items that have been rejected by the Product Owner (incomplete, broken etc.).  These should be reviewed an moved back to the Product Backlog board (Either Sprint Candidates or Ready).

Because we will be generating printed cards with this board cards in this board should, as much as possible, take the form described below,

<img src="http://yobriefca.se/images/blog/cardmocks.png" style="width:100%"/>

See [this blog post](http://yobriefca.se/blog/2012/12/28/trello-cards-materialising-trello-boards/) for an explanation about this.

__[Sprint Retrospective](https://trello.com/board/sprint-retrospective-1/5107b4ee7001c21621003df4)__

Feedback in its finest form.

__DON'T READ OTHER FEEDBACK BEFORE WRITING YOUR OWN__

For this activity to work, it's best that you contribute feedback before seeing what others have written.  If you want to hide other feedback, use the "search and filter cards" function on the far right, and search for cards assigned to you.  That should hide all cards, so you can begin writing without distractions. 

> At the end of each Sprint, the team meets for the Sprint Retrospective. In this meeting the team looks in detail at how things went during the Sprint. They determine and commit to specific action items to make things go better. The team may decide to add something to their process. Each addition is made with an eye to improving some specific aspect of things. Perhaps we found that we didn't understand clearly what some of the backlog items meant. We might decide to have testers attend the backlog refinement meeting, so that they can create executable examples of the items working correctly. Or we might find that our specialists are becoming bottlenecks, so we decide that all specialist work will be done with a non-specialist pair programming with a specialist. The team might decide to remove something from their process. Perhaps we find that time spent getting wireframes approved is holding us up, so we decide to just go ahead and build the actual windows simply, so that the UX people can see them, but the testers can start working on them as well.  Sometimes the team may feel that some aspect of Scrum itself is holding them up. When Scrum is hard to do, or seems to be slowing you down, the most likely case is that Scrum has identified an obstacle or impediment in your organization. Is it hard to get people together? That's telling you that you need to get people together, not that you need to figure out a way to limp along with people separated in time and space.

- Successes - Document items that made the sprint successful
- Less than Success - Document items that made the sprint less than successful
- Ah Ha! Moments - Mind Blowing moments you experienced during the sprint
- Appreciations - Mad props to people who deserve it
- Hindered - What hindered the sprint? 
- Hypothesis - What should we try next time?

## <a name="architecture"></a> Architecture

This is the current architectural vision

<img src="https://raw.github.com/DFID/aid-platform-beta/master/lib/images/architecture.png?login=kouphax&token=ab369cd411268e8e3188ec586921cb85" style="width:100%"/>

The technologies suggested are only theoretical and I expect them to change as and when the components evolve.  For now bare minimum spikes through the system are essential to draw out requirements.

- __CMS__ - The alpha provided configuration through a simple file.  The loader admin will be a more user friendly approach to defining what XML files should be loaded as well as providing an area to add "annotations" to the IATI data.
- __Loader__ - The loader pulls in the IATI XML data and loads it into the data stores of the API and Searcher
- __Searcher__ - Provides faceted, fuzzy searching of loaded IATI data.  This is only available to the Platform and not through the API.  The rationale for doing this is that it is very much geared towards the aid-platform format rather than arbitrary API calls.  We can consider exposing this externally as we go on.  Potentially built on top of ElasticSearch.
- __API__ - IATI Data API built against the working draft of the IATI API Standards.  Unlike the beta this wont be tailored specifically to DFIDs assumptions of the data shape.  Extra work will need to be carried out by the aid platform itself
- __Site__ - The aid platform will consume the API and generate a static site that can be hosted on any provider.

The following diagram is the current site map


<img src="http://yuml.me/bbad75cc" style="width:100%" />

[Edit This](http://yuml.me/edit/bbad75cc)

## <a name="development-guidelines"></a> Development Guidelines

### <a id="make-commits-meaningful"></a> Make commits meaningful

Keep the commit tree tidy and meaningful.  GDS have a a [great style guide for Git](https://github.com/alphagov/styleguides/blob/master/git.md).  Be descriptive - "fixed bugs" is not good enough :)

### <a id="dont-commit-to-master"></a> Dont commit to master

Use short lived feature branches to work in.  When something is ready to be merged into master [open a pull request](https://github.com/DFID/aid-platform-beta/pull/new/master) vis Github to allow the rest of the team to quickly review it before it goes into master.

<img src="https://raw.github.com/DFID/aid-platform-beta/master/lib/images/devflow.png?login=kouphax&token=32026e6e5f36038445c0164d0edcba1d" style="width:100%"/>

### <a id="run-all-tests-before-pushing"></a> Run all the tests before pushing

Don't rely on the CI server to make sure you haven't broken anything.  Be sure to run tests prior to committing and pushing code to Github.

## <a id="setup-notes"></a> Setup Notes

### <a id="necessary-environment-variables"></a> Environment Variables

Currently to make the application work end to end the varioous components use a number of environment variables that can be configured across the board.  These typically will point to a subfolder in the `aid-platform-build` submodule/repo within the project

- `DFID_DATA_PATH` - Points to the Ne4j Embedded DB path
- `DFID_ELASTICSEARCH_PATH` - Points to the ElasticSearch embedded DB path

These can be setup in you shells startup script eg.

	export DFID_DATA_PATH=/dfid/aid-platform-beta/data/iati-xml
	export DFID_ELASTICSEARCH_PATH=/dfid/aid-platform-beta/data/elasticsearch

## <a id="contributors"></a> Contributors

- John Adams (j-adams@dfid.gov.uk)
- Clare Maclennan (C-MacLennan@dfid.gov.uk)
- Virginia McLachlan (v-mclachlan@dfid.gov.uk)
- James Hughes (james@yobriefca.se)
- Kyle Davidson (k.davidson@kainos.com)
