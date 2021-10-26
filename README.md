# Finding a Concise, Precise, and Exhuastive Set of Near Bi-Cliques in Dynamic Graphs (WSDM'22)

This repository contains the source code for the paper [Finding a Concise, Precise, and Exhuastive Set of Near Bi-Cliques in Dynamic Graphs](https://github.com/hyeonjeong1/cutnpeel), by Hyeonjeong Shin, Taehyung Kwon, Neil Shah and Kijung Shin, to be presented at [WSDM 2022](https://www.wsdm-conference.org/2022/).

*In this work, we consider the problem of finding a concise, precise, and exhaustive set of near bi-cliques in a dynamic graph.
We formulate the problem as an optimization problem whose objective combines the three aspects (i.e., conciseness, preciseness, and exhaustiveness) in a systematic way based on the MDL principle.
Our algorithmic contribution is to design **CutNPeel** for the problem.
Compared to a widely-used top-down greedy search, **CutNPeel** reduces the search space and at the same time improves search accuracy, through a novel adaptive re-partitioning scheme.
We summarize the strengths of **CutNPeel** as follows:*
  * **High Quality** : CutNPeel provides near bi-cliques o up to 51.2% better quality than the second best one.
  * **Speed** : CutNPeel is up to 68.8× faster than the competitors that is second best in terms of quality.
  * **Scalability** : empirically, CutNPeel scales near linearly with the size of the input graph.
  * **Applicability** : CutNPeel is successfully applicable to lossless graph compression and interesting pattern discovery.

## Datasets
|Name|Description|Number of Objects|Number of Edges|Processed Dataset|Original Source|
|:---:|:---:|:---:|:---:|:---:|:---:|
|Enron|sender/receiver/time[week]|140/144/128|11,568|abc|
