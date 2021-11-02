# Finding a Concise, Precise, and Exhaustive Set of Near Bi-Cliques in Dynamic Graphs (WSDM'22)

This repository contains the source code for the paper [Finding a Concise, Precise, and Exhuastive Set of Near Bi-Cliques in Dynamic Graphs](https://github.com/hyeonjeong1/cutnpeel), by Hyeonjeong Shin, Taehyung Kwon, Neil Shah and Kijung Shin, to be presented at [WSDM 2022](https://www.wsdm-conference.org/2022/).

*In this work, we consider the problem of finding a concise, precise, and exhaustive set of near bi-cliques in a dynamic graph.
We formulate the problem as an optimization problem whose objective combines the three aspects (i.e., conciseness, preciseness, and exhaustiveness) in a systematic way based on the MDL principle.
Our algorithmic contribution is to design **CutNPeel** for the problem.
Compared to a widely-used top-down greedy search, **CutNPeel** reduces the search space and at the same time improves search accuracy, through a novel adaptive re-partitioning scheme.
We summarize the strengths of **CutNPeel** as follows:*
  * **High Quality** : CutNPeel provides near bi-cliques of up to 51.2% better quality than the second best one.
  * **Speed** : CutNPeel is up to 68.8× faster than the competitors that is second best in terms of quality.
  * **Scalability** : empirically, CutNPeel scales near linearly with the size of the input graph.
  * **Applicability** : CutNPeel is successfully applicable to lossless graph compression and interesting pattern discovery.

## Datasets
|Name|Description|Number of Objects|Number of<br />Edges|Processed<br />Dataset|Original<br />Source|
|:---:|:---:|:---:|:---:|:---:|:---:|
|Enron|sender / receiver / time [week]|140 / 144 / 128|11,568|[here](https://www.dropbox.com/sh/ag4ghglt04g7cg8/AADrC5OD7zQiPWdhDFpJWsuCa?dl=0)|[here](https://www.cs.cornell.edu/~arb/data/email-Enron/)
|Darpa|Src IP / Dst IP / time [day]|9,484 / 23,398 / 57|140,069|[here](https://www.dropbox.com/sh/ag4ghglt04g7cg8/AADrC5OD7zQiPWdhDFpJWsuCa?dl=0)|
|DDoS|Src IP / Dst IP / time [second]|9,312 / 9,326 / 3,954|22,844,324|[here](https://www.dropbox.com/sh/ag4ghglt04g7cg8/AADrC5OD7zQiPWdhDFpJWsuCa?dl=0)|[here](https://www.caida.org/catalog/datasets/ddos-20070804_dataset/#H2768)
|DBLP|author / venue / time [year]|418,236 / 3,566 / 49|1,325,416|[here](https://www.dropbox.com/sh/ag4ghglt04g7cg8/AADrC5OD7zQiPWdhDFpJWsuCa?dl=0)|
|Yelp|user / business / time [month]|552,339 / 77,079 / 134|2,214,201|[here](https://www.dropbox.com/sh/ag4ghglt04g7cg8/AADrC5OD7zQiPWdhDFpJWsuCa?dl=0)|[here](https://www.kaggle.com/yelp-dataset/yelp-dataset)
|Weeplaces|user / place / time [month]|15,793 / 971,308 / 92|3,970,922|[here](https://www.dropbox.com/sh/ag4ghglt04g7cg8/AADrC5OD7zQiPWdhDFpJWsuCa?dl=0)|[here](https://www.yongliu.org/datasets.html)

## How to Run
* To run demos, execute the following command:
```
./run_cutnpeel.sh example_data.txt 0.9 80 ./
```
* To run CutNPeel with a specified dataset and parameters, execute the following command:
```
./run_cutnpeel.sh input_path threshold_decrement iteration output_path
```
* For more details, please see the [user guide](https://github.com/hyeonjeong1/cutnpeel/blob/main/user_guide.pdf).

## Supplementary Document
Please see the [online appendix](https://github.com/hyeonjeong1/cutnpeel/blob/main/Online%20Appendix.pdf).
