# Contributing to this project

Thanks for considering contributing and help us on building this project!

The best way to contribute right now is to try things out and provide feedback, but we also accept contributions to the documentation and obviously to the code itself.

This document contains guidelines to help you get started and how to make sure your contribution gets accepted, making you our newest contributor!

## Communication channels

Should you have any questions or need some help in getting set up, you can use these communication channels to reach the team and get answers in a way where others can benefit from it as well:

- Github [Discussions](https://github.com/cardano-foundation/cf-lob/discussions)

## Your first contribution 

Contributing to the documentation, its translation, reporting bugs or proposing features are awesome ways to get started.

Also, take a look at the tests. Making sure we have the best high quality test suite is vital for this project.

### Documentation + translations

We host our documentation / user manual in the [Wiki](https://github.com/cardano-foundation/cf-log/wiki) and [README](./README.md).

### Bug reports

[Submit an issue](https://github.com/cardano-foundation/cf-lob/issues/new).

For bug reports, it's very important to explain
* what version you used,
* steps to reproduce (or steps you took),
* what behavior you saw (ideally supported by logs), and
* what behavior you expected.

### Feature ideas

Feature ideas are precursors to high-level features items, which will be discussed and fleshed out to ideally become items on our feature roadmap.

We use the [Ideas discussions category](https://github.com/cardano-foundation/cf-lob/discussions/categories/ideas) to discuss and vote on feature ideas, but you can also [submit an issue](https://github.com/cardano-foundation/cf-metadata-server/issues/new) using the "Feature idea :thought_balloon:" template and we convert that to a discussion.

We expect a description of
* why you (or the user) need/want something (e.g. problem, challenge, pain, benefit), and
* what this is roughly about (e.g. description of a new API endpoint or message format).

Note that we do NOT require a detailed technical description, but are much more interested in *why* a feature is needed. This also helps in understanding the relevance and ultimately the priority of such an item.

## Making changes

When contributing code, it helps to have discussed the rationale and (ideally) how something is implemented in a feature idea or bug ticket beforehand.

### Building & Testing

* Build with *Maven*
* Make sure **all** unit tests are successful
* Check the output of [PMD](https://pmd.github.io/) and [CPD](https://pmd.github.io/latest/pmd_userdocs_cpd.html) for any code quality issues with a priority higher than **4**

### Coding standards

Make sure to follow the [Goole style guide for Java](https://google.github.io/styleguide/javaguide.html) but more important check if the coding style you find is consistent and report or fix any inconsitencies by filing an issue or a pullrequest. Make sure to file a separate pullrequest.

In general regarding code style, just take a look at the existing sources and make your code look like them.

### Creating a pull request

Thank you for contributing your changes by opening a pull requests! To get something merged we usually require:
+ Description of the changes - if your commit messages are great, this is less important
+ Quality of changes is ensured - through new or updated automated tests
+ Change is related to an issue, feature (idea) or bug report - ideally discussed beforehand
+ Well-scoped - we prefer multiple PRs, rather than a big one

### Versioning & Changelog

During development
+ Make sure `CHANGELOG.md` is kept up-to-date with high-level, technical, but user-focused list of changes according to [keepachangelog](https://keepachangelog.com/en/1.0.0/)
+ Bump `UNRELEASED` version in `CHANGELOG.md` according to [semver](https://semver.org/)

### Releasing

To perform a release
+ Check version to be released is also correct in software components, e.g. `.pom` files.
+ Replace `UNRELEASED` with a date in [ISO8601](https://en.wikipedia.org/wiki/ISO_8601)
+ Create a signed, annotated git tag of the version: `git tag -as <version>`
+ (ideally) Use the released changes as annotation
