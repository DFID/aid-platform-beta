# Development Tracker

The Development Tracker allows you to find and explore detailed information on international development projects funded by the UK Government. The Development Tracker is managed by the [Department for International Development (DFID)](https://www.gov.uk/dfid).

The tracker is built using open data published by UK Government and partners, using the [International Aid Transparency Initiative (IATI) standard](http://iatistandard.org). The IATI standard is an international standard for international development data and allows ready comparison of information from different donors.

## Contents

1. [Introduction](#-introduction)
2. [Architectural Overview](#-architecture)
3. [Setup Notes](#-setup-notes)

## <a name="introduction"></a> Introduction

Beta version of the DFID Development Tracker

### <a name="useful-links"></a> Useful Links

- [IATI Standard](http://iatistandard.org) - provides detail on the IATI XML Standard and Validation rules and notes.
- [IATI Registry](http://iatiregistry.org) - provides searchable access ot all the published IATI files available.

## <a name="architecture"></a> Architectural Overview

The current architecture contains the following elements:

- __CMS__ - The alpha provided configuration through a simple file.  The loader admin is a more user friendly approach to defining what XML files should be loaded as well as providing an area to add "annotations" to the IATI data.
- __Loader__ - The loader pulls in the IATI XML data and loads it into the data stores of the API and Searcher
- __Searcher__ - Provides faceted, fuzzy searching of loaded IATI data.  This is only available to the Platform and not through the API.  The rationale for doing this is that it is very much geared towards the aid-platform format rather than arbitrary API calls.  We can consider exposing this externally as we go on.  Potentially built on top of ElasticSearch.
- __API__ - IATI Data API built against the working draft of the IATI API Standards.  Unlike the beta this wont be tailored specifically to DFIDs assumptions of the data shape.  Extra work will need to be carried out by the aid platform itself
- __Site__ - The aid platform will consume the API and generate a static site that can be hosted on any provider.

## <a id="setup-notes"></a> Setup Notes

All the setup instructions and how-to guides are in the [Wiki](wiki).

See [Setting up a Development Environment](wiki/Setting-up-a-Development-Environment)
