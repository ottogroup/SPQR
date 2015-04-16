# Introduction
The SPQR ("spooker") framework implements a dynamic framework for processing high volume data streams through pipelines. 
Established stream processing frameworks such as [Apache Storm](https://storm.apache.org/), [Apache Spark Streaming](http://spark.apache.org/) or [Apache Samza](http://samza.incubator.apache.org/) follow a static approach to building data stream processing pipelines:

1. pipelines are defined in code
2. the code goes through building and packaging
3. the resulting build artifacts are deployed on clusters
4. the implemented pipelines finally instantiated

This results in pipeline instantiation latencies realistically measured in minutes and not seconds. When a pipeline needs to be changed, the whole process has to be followed again with similar latency.

Such a static approach offers an adequate implementation model for long-running, stable, continuous ETL processes on data streams.

However, its intrinsic latencies make it unsuitable for pursuing a vision of near-real-time data analytics performed by human experts on near-real-time data prone to erode in value quickly that is usually delivered by data streams. Analysts should be able to process streaming data to gain insight - and once insight has been gained, to easily to refine the processing pipelines for even more insight or to switch or their focus of attention completely without much latency.

For example, an online marketing expert should be able to obtain live KPIs on customer activity going on in a shop right now, be able to drill into the activity of a single customer of interest, and then switch focus to different customers or different KPIs without much latency.

SPQR provides a foundation for such near-real-time data stream analytics, SPQR proposes an architecture that facilitates ad-hoc instantiation and modification of data stream processing pipelines with subsecond latency.

SPQR achieves this by separating the process of
* code deployment 
from the processes of 
* pipeline definition and instantiation.

SPQR allows for the code of data stream processing operators to be deployed to a cluster once, with similar latency as traditional frameworks. Once deployed however, these operators can be parameterized, instantiated, and (re)arranged into pipelines with subsecond latency as often as desired and permitted by cluster processing and memory capacity.

# Concepts
On a conceptual level the framework follows the notion of [pipelines](https://github.com/ottogroup/SPQR/wiki/Pipelines) which consumes data from a [data source](https://github.com/ottogroup/SPQR/wiki/Source), [processes](https://github.com/ottogroup/SPQR/wiki/Operator) it and [exports](https://github.com/ottogroup/SPQR/wiki/Emitter) the result into an attached destination. To form larger units these pipelines may be interconnected by exchanging data using a common [transportation layer](https://github.com/ottogroup/SPQR/wiki/Transportation-Layer).

# From ASAP to SPQR
The first version of SPQR ("spooker") was released under the acronym _ASAP_. Due to trademark issues it was later changed. From a technical perspective changing the name came along with some simplifications when it comes to pipeline setup. A number of generalization attempts were removed to simplify and speed up the use of SPQR in different context. 

For example, the mailbox is not a [pipeline component](https://github.com/ottogroup/SPQR/wiki/SPQR-component-annotation) anymore that may be replaced by customized implementation on the fly but now uses the only available implementation based on [chronicles](https://github.com/OpenHFT/Java-Chronicle). Although it restricts flexibility it reduces complexity and leads to faster deployment processes. 


# Building Blocks

![SPQR architecture overview](https://github.com/ottogroup/SPQR/blob/master/architecture-overview.jpg)

The framework is built up from three core elements: (1) [resource management](https://github.com/ottogroup/SPQR/wiki/Resource-Manager), (2) [processing nodes](https://github.com/ottogroup/SPQR/wiki/Processing-Node) and (3) a layer used for [message transportation](https://github.com/ottogroup/SPQR/wiki/Transportation-Layer) (we use [Apache Kafka](http://kafka.apache.org) as default layer, but it may be replaced at will). No other components are required to set up a working SPQR cluster.

## Processing Nodes
The [processing nodes](https://github.com/ottogroup/SPQR/wiki/Processing-Node) provide a runtime environment for [pipelines](https://github.com/ottogroup/SPQR/wiki/Pipelines) which are deployed by the resource management depending on available resources. 

## Resource Management
The [resource management](https://github.com/ottogroup/SPQR/wiki/Resource-Manager) keeps track of all processing nodes and their resources. It distributes component libraries and pipelines into the cluster.

## Transportation Layer
Generally the framework is able to consume any type of source and write to any type of destination. But when it comes to interconnecting pipelines it is recommended to use a Apache Kafka setup. Such a configuration is already in production use and has proven its stability as well as its reliability. To learn more about how to configure and use the kafka infrastructure which comes with the framework please read the [transportation layer](https://github.com/ottogroup/SPQR/wiki/Transportation-Layer) documentation.

# Download SPQR to Get Started
Download the SPQR software package following the [download &amp; build instructions](https://github.com/ottogroup/SPQR/wiki/Build-and-Deployment-instructions) and set up a single node SPQR cluster.

# Tutorials
To learn how to use and extend a new framework the best way is to use it hands-on. Therefore the wiki provides a set of [tutorials](https://github.com/ottogroup/SPQR/wiki/Tutorials) showing how to use and extend the framework.


