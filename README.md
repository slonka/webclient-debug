### webclient-debug
Trying to figure out why first request using WebClient takes so much time.

### Backstory:
When we started using webclient in our library and we've noticed that the CI builds failed because of a 500ms timeout.
We narrowed it down to first WebClient request and started digging what's happening.

### Running
If you run `restTemplateRequest()` test in `WebClientFirstRequestTest` you will see the output:

```
First took: 46 ms
Second took: 5 ms
Difference in time: 40 ms
```

Which means that the first request to a `WireMock` server took `46 ms` and the second request took `5 ms`.
This test uses `RestTemplate` to perform the request.

If you run `webclientRequest()` the first request takes `316 ms` and the second `6 ms`.

Creating a new `WebClient` instance doesn't mean that the first request in the new instance will be slow.
Running `webclientRequestFromDifferentInstances` outputs:

```
block(): 319 ms
block(): 8 ms
block(): 6 ms
block(): 6 ms
```

This means that the time is mainly spent in creating shared state, meaning `static` fields initialization.

I wanted to profile this code to see which functions take the most time, it's only done on the first call so it cannot be
run in a loop and profiled with something like [async-profiler](https://github.com/jvm-profiling-tools/async-profiler).

In order to measure call time I wrote a java agent. It instruments all the methods and inserts timing information.
See `CallSpy` and `Timer`.

That produces an output in which `>` means entering a function. `<` means returning from function.
That is followed by function name and time it took to run it.
You can see `first_call.txt` and `second_call.txt` and `diff.html` (this is just a convenience file - you can do your diff manually) to see the differences between first and second `WebClient` call.

You can find sorted results in a file `function timing - averages (100 samples).tsv`.

Bear in mind that the time of a function lower on the stack is a sum of all the functions above it.
For example:

```
>a()
 >b()
 <b() 50ms
<a() 51ms
```

means that function `a()` probably* took `~1ms` and the heavy function might* be `b()` (* - I found that `javasisst` can't instrument all the functions so there might be somewhat in between calls);

After analyzing the diff, function stacks and trying to remove functions that are just* "wrappers" I think that the most time consuming functions are:
- ReactorClientHttpConnector.adaptRequest
- ClientRequest.create
- BodyExtractors.readWithMessageReaders (which is a part of bodyToMono)
- AbstractDataBufferDecoder.decodeToMono (which is a part of bodyToMono)
- DefaultUriBuilderFactory$DefaultUriBuilder.createUri
- HierarchicalUriComponents.encode(java.nio.charset.Charset)

I tried to mitigate the long times by creating a `preload` method that uses some of the classes but this had only limited effect,
I was able to reduce firsts request time by `~80 ms`.
Try running `webclientRequestWithPreload` and `webclientRequest` test to see the effects.
On my machine the times are around `345 ms` and `257 ms`.

### Summary

Is there a way to work around this issue?
Should I just perform a request to a mocked server before starting the test suite?
