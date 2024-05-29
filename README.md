# Acid
A simple anticheat that includes a perfect 3.00001 reach check (1.8.9) using bruteforce positions. This is purely a proof of concept.

If you have any issues you can contact me on [telegram](https://t.me/beaness) or on [discord (@1.7.10)](https://discord.com/users/261885314933587969)

**Note:** this is not production ready, when someone lags within a transaction sandwich the tracked branches grow exponentially. There are no checks / limitations for this which means running this in production can easily crash your server. A solution for this is using bounding boxes instead of splitting into branches.