package com.example.data

object DataProvider {
    val initialManuscripts = listOf(
        Manuscript(
            title = "Chopin: Nocturnes Op. 9",
            composer = "Frédéric Chopin",
            category = "Piano",
            coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDhXbQWe85IvtEXm2Jr2Z8sMBSRzTaT8YFIPCACCo3DIvVEICiqbeNLgsgEoIWnhKv2lW4bMqVrJrkgywOJsLfcldyKEePIBARMi7lcXYi6OnlHHxUM9fIO5OJrB7SJ_Ca81or--1gaCNwhuepHH_a90iQLSynRpLe-Rafc3_0MqpETAAUeVFP-rT64sWav0x77Xzof2FyeUNLQNM2hKIHD4QLdrB-1GaAENjPWbzNX-_UBpvQIRZzs4O_iYgYs8_dFcsV8503oT4nc",
            isFavorite = true,
            lastUsedTimestamp = System.currentTimeMillis() - 100000,
            keySignature = "Eb Major",
            era = "Romantic"
        ),
        Manuscript(
            title = "The Real Jazz Book",
            composer = "Various",
            category = "Jazz",
            coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDsP1ZpAfL7s-vQyHfF5jbZ7byw2u66z2WQN8oMCsfVjIHOSV-nHbUnTQ0gWQIreWzV-eyLxW190vRnl19yDaT8sajtaS799Q8uWh048V6Df1GvwPsregp0wZc1zG9uleUtjGzu_FwpvieHHiKbfEx7jkuVBl0rIVFRi5hUXunTmKLQDElX4hVMLUSaX2cr9I5VIqJt1IMVcaulBU4tQMpWDshO8gkXhnEMmKzBTCD_k7JvCVgDBBN6TbovVeQ4XB0rFGE_aryjTKWw",
            isFavorite = false,
            lastUsedTimestamp = System.currentTimeMillis() - 200000,
            keySignature = "Various",
            era = "Modern"
        ),
        Manuscript(
            title = "Bach: Well-Tempered Clavier",
            composer = "Johann Sebastian Bach",
            category = "Piano",
            coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDkXEDLXt54WGAJwb-7Y4mtbRu4HngOlQj-4amSro2n4NGyOfwVRC-efQXwA3HxRKkYdczdpCkRD3pVLsqV7WxdTGqFbVfcdyG12uH5i-CUiLVyIJQM1i2Kn5_WIWsn7g5VyKQnAJ7AxAfr69WpeGAXQt74qNeaFykm6sYfs-bVBpk11uCiyWm05-HlL_kXzkl3nJLSoyHgCgWuc7wlLTen6_fz2Qd4ZLmv3Iu7qyggfMOqN2liFyMmCxEFaZNXh22fVD128-X3XG6u",
            isFavorite = true,
            lastUsedTimestamp = System.currentTimeMillis() - 300000,
            keySignature = "C Major",
            era = "Baroque"
        ),
        Manuscript(
            title = "Cinematic Themes",
            composer = "Various",
            category = "Piano",
            coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCzvpJ7Yr3LWkIdSJTA61XsVdlDh2HH4jK_8UzjyHk6geLVjbPy7iTG39mFLKsVTJykIccIsWBHvqyICRDOyZMkiPwPfGeLkAxwcOgx755-nilepL7wohoBca9mv_YLiVxRDZkffXiiOmWNzjPTfaL2hXTKHf2xcFDC1zxMbjaP_QOja8QAiEkiw0H4NEEMG4_Hg5pj1tfAHh9PVyJqF6w5yGk24CmS6m_JW6Fjy5k5ymM-ARXbFXh9d8eaKK9Zm3cm3iUUrGjx2aKX",
            isFavorite = false,
            lastUsedTimestamp = System.currentTimeMillis() - 400000,
            keySignature = "Various",
            era = "Film"
        ),
        Manuscript(
            title = "Beethoven: Moonlight Sonata",
            composer = "Ludwig van Beethoven",
            category = "Piano",
            coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDM9EKuHQSI-eVm5s2bMe0jHMdKRcIEUv0uLN_3pDCdnx38Rjxysk-AXZoMI8V8oJRcIDWofvqD8SjWdfJp-T8KpdMiQeAQTj9UoCeo3KVPlZSRGftHWZ8fXvRrbK3qTpbUJNaX0biof99g04xPOltpFfxwgSGWZbHKJAycFT3yAynksglFmNuNkQIHAG7MoVrjj-xJzlLjoGL3hrzkRIQjFkR61x2GO_hiiEY9ul90MBOxTyxD58HoB7fGpAHEY7RibAusiRHlx3TS",
            isFavorite = true,
            lastUsedTimestamp = System.currentTimeMillis() - 500000,
            keySignature = "C# Minor",
            era = "Classical"
        ),
        Manuscript(
            title = "Debussy: Préludes",
            composer = "Claude Debussy",
            category = "Piano",
            coverUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAxyTe8yyAZOodLo3Y04ylYLb6pWs1wz3i5wfvIjAUix8mMgP7aTJvBBMBY5guxokHBxYcT4IoF7RYduEIxvVBpZHuU75JH1jddktNpfh4sY25TVs0Y35jQVunAlmrLy0UiBeDm8Dh6CQLSjQd8SwsFZmSmoUgDbvE9ACjLU21HcsBkNjKpfkkKhGf76krAlzdfG38xnQBmL7heQvcgAShqOe0C9u9G1hS7rmjXEaUc1VwcEFIZ6SsxTvzFTZrIOnKJn3wO1zQvn-pw",
            isFavorite = false,
            lastUsedTimestamp = System.currentTimeMillis() - 600000,
            keySignature = "Various",
            era = "Impressionist"
        )
    )
    
    val readerPages = listOf(
        "https://lh3.googleusercontent.com/aida-public/AB6AXuB6gWQYNiN9_KFrsZL9LnHn4OXTng4fmjg4M0GXuSHPD94XWSEFsuubEj52s2kAYOL7Bn-RYKOtb9sLSkjhA0L9YBVCbpODna5GcSt-xouf4UWX75JOuZDC4hZwVCfgKp6Lq6qEJfA5Pcx52pCLWGr_fD9CNm-6hl8txT8Mwxlxm4Yh2ePD9mpzPNJpRZEpZpUJNcNo8IKUHc0TUT9I1R5Bq1roDsev7oFdz1mwO6Kd_ryADR8ItLr7IcrFEvmgWvhn2iBiHF27lcxd",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuAL_drmDz42Pr_XvEFpV_D0yJyGVeJMZ3B6qAV95ODxJIuCEI2ZdYCAKmjLodqFqyu_ICPHNj-vOowqoxKQwEJpqsmwL6GMXMF4XTLWVR_E_aIUGiP6Hw-nuQW7XVJWwz_lqfR52A76BSDKSX7gVMvu25ikPZaC7v6qpU-5n77bOYufle5ltUVe4BjkVRi7Zf9YzuVcEp3DDMZ8yzyVBfipijkLFYXOwfyflJ-PUjZENGHG9nLk7J8l2-HMAvOZibi_V8nXCF88O4GG",
        "https://lh3.googleusercontent.com/aida-public/AB6AXuBHQ-TZS2NgADUZwt5XTmCpzy6c6JKdGDc8kb0UbMSyM1jkO-TrF4j83kBaYAHBGOk8s3HxZ5mzy4t1ciquHqoq0v102TLMcg1fBLgLQihc5Ftb8IBYkjee7TxIPwHjSQucy-WDHZYCKoMBZsZbddyprW9_iU0Yec4aHFpkIEv4mKNm0WHbAj70u1KSeMO7KYmA0LY9JEUSFvM228NP8x9-QX6xVkO08Xi88KUCDCPhNuZQ3LIO7j_yy_aKPlJxd0QWTcRYRppBjqKh"
    )
}
