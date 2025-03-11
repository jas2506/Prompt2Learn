import { useState, useCallback } from "react";
import axios, { AxiosRequestConfig, AxiosResponse } from "axios";

// Directly define the API base URL without using an env file.
const apiUrl = "http://localhost:9091";

// Set the base URL for all axios requests.
axios.defaults.baseURL = apiUrl;

const useApiWithAuth = () => {
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<unknown>(null);

    const apiCall = useCallback(
        async (
            method: string,
            url: string,
            data: any = null,
            config: AxiosRequestConfig = {}
        ): Promise<AxiosResponse<any>> => {
            setLoading(true);
            setError(null);
            try {
                const response = await axios({ method, url, data, ...config });
                setLoading(false);
                return response;
            } catch (err: unknown) {
                setError(err);
                setLoading(false);
                throw err;
            }
        },
        []
    );

    return { apiCall, loading, error, clearError: () => setError(null) };
};

export default useApiWithAuth;
